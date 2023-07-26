package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.EleEsignConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleUserEsignRecordMapper;
import com.xiliulou.electricity.mapper.EleUserIdentityAuthRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityEsignConfigMapper;
import com.xiliulou.electricity.query.SignFileQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.SignUtils;
import com.xiliulou.electricity.vo.CreateFileVO;
import com.xiliulou.electricity.vo.SignFlowVO;
import com.xiliulou.esign.config.EsignConfig;
import com.xiliulou.esign.constant.EsignConstant;
import com.xiliulou.esign.entity.query.ComponentData;
import com.xiliulou.esign.entity.query.EsignCallBackQuery;
import com.xiliulou.esign.entity.query.SignFlowDataQuery;
import com.xiliulou.esign.entity.query.UserInfoQuery;
import com.xiliulou.esign.entity.resp.*;
import com.xiliulou.esign.service.ElectronicSignatureService;
import com.xiliulou.esign.service.PersonalAuthenticationService;
import com.xiliulou.esign.service.SignatureFileService;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.impl.AliyunOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author: Kenneth
 * @Date: 2023/7/8 15:14
 * @Description:
 */

@Service
@Slf4j
public class EleCabinetSignatureServiceImpl implements EleCabinetSignatureService {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    RedisService redisService;
    @Autowired
    AliyunOssService aliyunOssService;
    @Autowired
    StorageConfig storageConfig;
    @Autowired
    ElectricityConfigService electricityConfigService;
    @Autowired
    EleEsignConfigService eleEsignConfigService;
    @Autowired
    PersonalAuthenticationService personalAuthenticationService;
    @Autowired
    ElectronicSignatureService electronicSignatureService;
    @Autowired
    SignatureFileService signatureFileService;
    @Autowired
    EsignCapacityDataService esignCapacityDataService;
    @Autowired
    private EsignConfig esignConfig;
    @Autowired
    private EleUserIdentityAuthRecordMapper eleUserIdentityAuthRecordMapper;
    @Autowired
    private EleUserEsignRecordMapper eleUserEsignRecordMapper;
    @Autowired
    private ElectricityEsignConfigMapper esignConfigMapper;

    private static ExecutorService savePsnAuthResultExecutor = XllThreadPoolExecutors.newFixedThreadPool("savePsnAuthResult",
            2, "SAVE_PSN_AUTH_RESULT");

    @Deprecated
    @Override
    public Triple<Boolean, String, Object> personalAuthentication() {

        if (!redisService
                .setNx(CacheConstant.CACHE_ELE_CABINET_ESIGN_AUTH_LOCK_KEY + SecurityUtils.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "000000", "操作频繁，请稍后再试！");
        }

        //获取当前用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "000100", "未找到用户!");
        }

        //用户是否被限制
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE ERROR! user is unUsable,uid={}", userInfo.getUid());
            return Triple.of(false, "000101", "用户已被禁用!");
        }

        //对应的租户是否已经开启电子签署功能
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR! electricityConfig is null,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "000102", "系统异常！");
        }

        //未启用该功能，则正常进行其他操作
        if (!Objects.equals(EleEsignConstant.ESIGN_ENABLE, electricityConfig.getIsEnableEsign())) {
            log.error("ELE ERROR! not open face recognize,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(true, "", "电子签名功能未启用！");
        }

        //查询当前用户所属的租户签名配置信息
        EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfig)
                || StringUtils.isBlank(eleEsignConfig.getAppId())
                || StringUtils.isBlank(eleEsignConfig.getAppSecret())) {
            log.error("ELE ERROR! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "000104", "租户电子签名配置信息不存在!");
        }

        //开始进行个人信息认证。先获取个人认证链接
        //检查当前用户是否已经完成个人认证。新用户无法直接通过手机信息去查询个人验证信息。通过数据库记录进行检查。
        EleUserIdentityAuthRecord eleUserIdentityAuthRecord = eleUserIdentityAuthRecordMapper.selectLatestAuthRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
        if(Objects.nonNull(eleUserIdentityAuthRecord) && eleUserIdentityAuthRecord.getRealNameStatus() == 1){
            PsnAuthLinkData psnAuthLinkData = new PsnAuthLinkData();
            psnAuthLinkData.setAuthFlowId(eleUserIdentityAuthRecord.getAuthFlowId());
            return Triple.of(true, "用户已经实名认证完成", psnAuthLinkData);
        }

        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setPhone(userInfo.getPhone());
        userInfoQuery.setUserName(userInfo.getName());

        return getAuthLink(userInfoQuery);
    }

    private Triple<Boolean, String, Object> getAuthLink(UserInfoQuery userInfoQuery){
        PsnAuthLinkResp psnAuthLinkResp = null;
        try {
            psnAuthLinkResp = personalAuthenticationService.queryPsnAuthLink(userInfoQuery, esignConfig.getRedirectUrlAfterAuth(), esignConfig.getXxlAppId(), esignConfig.getXxlAppSecret());
        } catch (Exception e) {
            log.error("get personal auth link error!", e);
            return Triple.of(false, "000105", "签署流程获取用户认证信息失败！");
        }
        return Triple.of(true, "", psnAuthLinkResp.getData());
    }

    @Override
    public Triple<Boolean, String, Object> createFileByTemplate(){
        if (!redisService
                .setNx(CacheConstant.CACHE_ELE_CABINET_ESIGN_CREATE_FILE_LOCK_KEY + SecurityUtils.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "000000", "操作频繁，请稍后再试！");
        }
        UserInfo userInfo = null;
        CreateFileVO createFileVO = new CreateFileVO();

        try{
            //获取当前用户信息
            userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
            if (Objects.isNull(userInfo)) {
                log.error("Create File error! not found userInfo,uid={}", SecurityUtils.getUid());
                return Triple.of(false, "000100", "未找到用户");
            }

            //用户是否被限制
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("Create File error! user is unUsable,uid={}", userInfo.getUid());
                return Triple.of(false, "000101", "用户已被禁用");
            }

            //用户是否审核通过
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("Create File error! userinfo is UN AUTH! uid={}", userInfo.getUid());
                return Triple.of(false, "100109", "用户未审核");
            }

            //获取用户所属租户的签名配置信息
            EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
            if (Objects.isNull(eleEsignConfig)
                    || StringUtils.isBlank(eleEsignConfig.getAppId())
                    || StringUtils.isBlank(eleEsignConfig.getAppSecret())
                    || StringUtils.isBlank(eleEsignConfig.getDocTemplateId())) {
                log.error("Create File error! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                        TenantContextHolder.getTenantId());
                return Triple.of(false, "000104", "租户电子签名配置信息不存在");
            }

            //校验租户签名次数
            EsignCapacityData esignCapacityData = esignCapacityDataService.queryCapacityDataByTenantId(TenantContextHolder.getTenantId().longValue());
            if (Objects.isNull(esignCapacityData)) {
                log.error("Create File error! eSign capacity data is null, uid={}, tenantId={}", SecurityUtils.getUid(), TenantContextHolder.getTenantId());
                return Triple.of(false, "000106", "未购买签名资源包，请联系管理员");
            }
            if (esignCapacityData.getEsignCapacity() <= EleEsignConstant.ESIGN_MIN_CAPACITY) {
                log.error("Create File error! eSign capacity is not enough,uid={}, tenantId={}", SecurityUtils.getUid(), TenantContextHolder.getTenantId());
                return Triple.of(false, "000107", "签名资源包余额不足，请联系管理员");
            }

            //根据模板id创建签署文件
            List<ComponentData> componentDataList = new ArrayList<>();
            FileCreateByTempResp fileCreateByTempResp = signatureFileService.createFileByTemplate(eleEsignConfig.getDocTemplateId(), eleEsignConfig.getSignFileName(),componentDataList, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
            String fileId = fileCreateByTempResp.getData().getFileId();

            //根据模版ID获取组件位置
            SignComponentResp signComponentResp = signatureFileService.findComponentsLocation(eleEsignConfig.getDocTemplateId(),eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
            ComponentPosition componentPosition = signComponentResp.getData().getComponents().get(0).getComponentPosition();

            createFileVO.setFileId(fileId);
            createFileVO.setComponentPageNum(componentPosition.getComponentPageNum());
            createFileVO.setComponentPositionX(componentPosition.getComponentPositionX());
            createFileVO.setComponentPositionY(componentPosition.getComponentPositionY());

        }catch(Exception e){
            log.error("Create File error! create file by template error,uid={},ex={}", userInfo.getUid(), e);
            return Triple.of(false, "000110", "根据模板创建文件失败");

        }finally {
            redisService.delete(CacheConstant.CACHE_ELE_CABINET_ESIGN_CREATE_FILE_LOCK_KEY + SecurityUtils.getUid());
        }

        return Triple.of(true, "", createFileVO);
    }

    @Override
    public Triple<Boolean, String, Object> getSignFlowLink(SignFileQuery signFileQuery) {

        if (!redisService
                .setNx(CacheConstant.CACHE_ELE_CABINET_ESIGN_SIGN_LOCK_KEY + SecurityUtils.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "000000", "操作频繁，请稍后再试！");
        }

        String signFlowId = null;
        UserInfo userInfo = null;

        try{
            userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
            if (Objects.isNull(userInfo)) {
                log.error("get sign flow link error! not found userInfo,uid={}", SecurityUtils.getUid());
                return Triple.of(false, "000100", "未找到用户");
            }

            //用户是否被限制
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("get sign flow link error! user is unUsable,uid={}", userInfo.getUid());
                return Triple.of(false, "000101", "用户已被禁用");
            }

            //用户是否审核通过
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("get sign flow link error! userinfo is UN AUTH! uid={}", userInfo.getUid());
                return Triple.of(false, "100109", "用户未审核");
            }

            //获取用户所属租户的签名配置信息
            EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
            if (Objects.isNull(eleEsignConfig)
                    || StringUtils.isBlank(eleEsignConfig.getAppId())
                    || StringUtils.isBlank(eleEsignConfig.getAppSecret())
                    || StringUtils.isBlank(eleEsignConfig.getDocTemplateId())) {
                log.error("get sign flow link error! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                        TenantContextHolder.getTenantId());
                return Triple.of(false, "000104", "租户电子签名配置信息不存在");
            }

            //校验租户签名次数
            EsignCapacityData esignCapacityData = esignCapacityDataService.queryCapacityDataByTenantId(TenantContextHolder.getTenantId().longValue());
            if (Objects.isNull(esignCapacityData)) {
                log.error("get sign flow link error! eSign capacity data is null, uid={}, tenantId={}", SecurityUtils.getUid(), TenantContextHolder.getTenantId());
                return Triple.of(false, "000106", "未购买签名资源包，请联系管理员");
            }
            if (esignCapacityData.getEsignCapacity() <= EleEsignConstant.ESIGN_MIN_CAPACITY) {
                log.error("get sign flow link error! eSign capacity is not enough,uid={}, tenantId={}", SecurityUtils.getUid(), TenantContextHolder.getTenantId());
                return Triple.of(false, "000107", "签名资源包余额不足，请联系管理员");
            }

            String fileName = eleEsignConfig.getSignFileName();

            //基于文件发起签署流程
            UserInfoQuery userInfoQuery = new UserInfoQuery();
            userInfoQuery.setPhone(userInfo.getPhone());
            userInfoQuery.setUserName(userInfo.getName());

            SignFlowDataQuery signFlowDataQuery = new SignFlowDataQuery();
            signFlowDataQuery.setFileId(signFileQuery.getFileId());
            signFlowDataQuery.setSignFileName(fileName);
            signFlowDataQuery.setSignFlowName(eleEsignConfig.getSignFlowName());
            signFlowDataQuery.setTenantAppId(eleEsignConfig.getAppId());
            signFlowDataQuery.setTenantAppSecret(eleEsignConfig.getAppSecret());
            signFlowDataQuery.setRedirectUrl(esignConfig.getRedirectUrlAfterSign());
            signFlowDataQuery.setNotifyUrl(esignConfig.getSignFlowNotifyUrl() + eleEsignConfig.getId());

            signFlowDataQuery.setPositionPage(String.valueOf(signFileQuery.getComponentPageNum()));
            signFlowDataQuery.setPositionX(signFileQuery.getComponentPositionX());
            signFlowDataQuery.setPositionY(signFileQuery.getComponentPositionY());

            //signFlowVO = getSignFlowResp(userInfo.getUid(), userInfoQuery, signFlowDataQuery);
            signFlowId = getSignFlowId(userInfo.getUid(), userInfoQuery, signFlowDataQuery);

        }catch(Exception e){
            log.error("get sign flow link error! get sign flow link error,uid={},ex={}", userInfo.getUid(), e);
            return Triple.of(false, "000109", "实名认证信息与当前签署人信息不符");
        }finally {
            redisService.delete(CacheConstant.CACHE_ELE_CABINET_ESIGN_SIGN_LOCK_KEY + SecurityUtils.getUid());
        }

        return Triple.of(true, "", signFlowId);
    }

    public String getSignFlowId(Long uid, UserInfoQuery userInfoQuery, SignFlowDataQuery signFlowDataQuery){
        String signFlowId = StringUtils.EMPTY;
        //基于文件发起签署流程, 每发起一次签署流程都需要计费，则需要判断之前是否有发起过签署。如果有，则从数据库中拿出signFlowId
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordMapper.selectLatestEsignRecordByUser(uid, TenantContextHolder.getTenantId().longValue());
        if(Objects.nonNull(eleUserEsignRecord)){
            log.info("Signing process already exist, sign flow id: {}", eleUserEsignRecord.getSignFlowId());
            signFlowId = eleUserEsignRecord.getSignFlowId();

            //根据signFlowId获取psnId信息，并获取有效期信息。
            SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, signFlowDataQuery.getTenantAppId(), signFlowDataQuery.getTenantAppSecret());
            Long expiredTime = signFlowDetailResp.getData().getSignFlowConfig().getSignFlowExpireTime();
            if(System.currentTimeMillis() < expiredTime){
                return signFlowId;
            }
        }

        //数据库中没有记录，则基于文件发起新的签署流程
        SignDocsCreateResp signDocsCreateResp = electronicSignatureService.createByFileFlow(userInfoQuery, signFlowDataQuery);
        signFlowId = signDocsCreateResp.getData().getSignFlowId();
        log.info("create new signing process, sign flow id: {}", signFlowId);

        //创建新的签署流程记录
        createUserEsignRecord(uid, signFlowId, signFlowDataQuery.getFileId(), signFlowDataQuery.getSignFileName());
        return signFlowId;
    }

    @Override
    public Triple<Boolean, String, Object> getSignFlowUrl(String signFlowId){
        SignFlowVO signFlowVO = new SignFlowVO();

        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());

        if (Objects.isNull(userInfo)) {
            log.error("get sign flow url error! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "000100", "未找到用户");
        }

        //获取用户所属租户的签名配置信息
        EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfig)
                || StringUtils.isBlank(eleEsignConfig.getAppId())
                || StringUtils.isBlank(eleEsignConfig.getAppSecret())
                || StringUtils.isBlank(eleEsignConfig.getDocTemplateId())) {
            log.error("get sign flow url error! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "000104", "租户电子签名配置信息不存在");
        }

        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setPhone(userInfo.getPhone());
        userInfoQuery.setUserName(userInfo.getName());

        SignFlowDataQuery signFlowDataQuery = new SignFlowDataQuery();
        signFlowDataQuery.setRedirectUrl(esignConfig.getRedirectUrlAfterSign());
        signFlowDataQuery.setTenantAppId(eleEsignConfig.getAppId());
        signFlowDataQuery.setTenantAppSecret(eleEsignConfig.getAppSecret());

        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        signFlowVO.setPsnId(signFlowDetailResp.getData().getSigners().get(0).getPsnSigner().getPsnId());

        SignFlowUrlResp signFlowUrlResp = electronicSignatureService.querySignFlowLink(signFlowId, userInfoQuery, signFlowDataQuery);
        signFlowVO.setUrl(signFlowUrlResp.getData().getUrl());
        signFlowVO.setShortUrl(signFlowUrlResp.getData().getShortUrl());
        signFlowVO.setSignFlowId(signFlowId);

        return Triple.of(true, "", signFlowVO);
    }

    @Deprecated
    public SignFlowVO getSignFlowResp(Long uid, UserInfoQuery userInfoQuery, SignFlowDataQuery signFlowDataQuery){
        SignFlowVO signFlowVO = new SignFlowVO();

        //基于文件发起签署流程, 每发起一次签署流程都需要计费，则需要判断之前是否有发起过签署。如果有，则从数据库中拿出signFlowId
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordMapper.selectLatestEsignRecordByUser(uid, TenantContextHolder.getTenantId().longValue());
        if(Objects.nonNull(eleUserEsignRecord)){
            log.info("Signing process already exist, sign flow id: {}", eleUserEsignRecord.getSignFlowId());
            String signFlowId = eleUserEsignRecord.getSignFlowId();

            //根据signFlowId获取psnId信息，并获取有效期信息。
            SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, signFlowDataQuery.getTenantAppId(), signFlowDataQuery.getTenantAppSecret());
            Long expiredTime = signFlowDetailResp.getData().getSignFlowConfig().getSignFlowExpireTime();
            if(System.currentTimeMillis() < expiredTime){
                signFlowVO.setSignFlowId(signFlowId);
                signFlowVO.setPsnId(signFlowDetailResp.getData().getSigners().get(0).getPsnSigner().getPsnId());

                //获取文件签署链接
                SignFlowUrlResp signFlowUrlResp = electronicSignatureService.querySignFlowLink(signFlowId, userInfoQuery, signFlowDataQuery);
                signFlowVO.setUrl(signFlowUrlResp.getData().getUrl());
                signFlowVO.setShortUrl(signFlowUrlResp.getData().getShortUrl());

                return signFlowVO;
            }
        }

        //数据库中没有记录，则基于文件发起新的签署流程
        SignDocsCreateResp signDocsCreateResp = electronicSignatureService.createByFileFlow(userInfoQuery, signFlowDataQuery);
        String signFlowId = signDocsCreateResp.getData().getSignFlowId();
        log.info("create new signing process, sign flow id: {}", signFlowId);
        signFlowVO.setSignFlowId(signFlowId);

        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, signFlowDataQuery.getTenantAppId(), signFlowDataQuery.getTenantAppSecret());
        signFlowVO.setPsnId(signFlowDetailResp.getData().getSigners().get(0).getPsnSigner().getPsnId());

        SignFlowUrlResp signFlowUrlResp = electronicSignatureService.querySignFlowLink(signFlowId, userInfoQuery, signFlowDataQuery);
        signFlowVO.setUrl(signFlowUrlResp.getData().getUrl());
        signFlowVO.setShortUrl(signFlowUrlResp.getData().getShortUrl());
        //创建新的签署流程记录
        createUserEsignRecord(uid, signFlowId, signFlowDataQuery.getFileId(), signFlowDataQuery.getSignFileName());
        return signFlowVO;
    }

    @Deprecated
    private void savePsnAuthResult(UserInfo userInfo, PsnAuthDetailResp psnAuthDetailResp){

        savePsnAuthResultExecutor.execute(() -> {
            //将认证后的信息更新至数据库
            createUserIdentityAuthRecord(psnAuthDetailResp, userInfo.getUid());
            FaceRecognitionInfoResp faceRecognitionInfo = psnAuthDetailResp.getData().getAuthInfo().getPerson().getFaceRecognitionInfo();
            if(Objects.isNull(faceRecognitionInfo) && StringUtils.isEmpty(faceRecognitionInfo.getFacePhotoUrl())){
                return;
            }
            String downLoadUrl = faceRecognitionInfo.getFacePhotoUrl();
            String pshPhotoPath = EleEsignConstant.PSN_PHOTO_OSS_PATH + userInfo.getPhone() +"_"+ userInfo.getUid() + ".png";
            URL url = null;
            HttpURLConnection conn = null;
            InputStream inputStream = null;
            try {
                url = new URL(downLoadUrl);
                conn = (HttpURLConnection) url.openConnection();
                inputStream = conn.getInputStream();
                aliyunOssService.uploadFile(storageConfig.getBucketName(), pshPhotoPath, inputStream);

            } catch (Exception e) {
                log.error("Save personal auth result fail, uid = {},result = {}", userInfo.getUid(), JsonUtil.toJson(psnAuthDetailResp));
            }finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.close(conn);
            }
        });
    }

    @Override
    public Triple<Boolean, String, Object> checkUserEsignFinished() {
        //获取当前用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "000100", "未找到用户");
        }

        //用户是否被限制
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE ERROR! user is unUsable,uid={}", userInfo.getUid());
            return Triple.of(false, "000101", "用户已被禁用");
        }

        //对应的租户是否已经开启电子签署功能
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR! electricityConfig is null,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "000102", "系统异常！");
        }

        //未启用该功能，则正常进行其他操作
        if (!Objects.equals(EleEsignConstant.ESIGN_ENABLE, electricityConfig.getIsEnableEsign())) {
            log.error("ELE ERROR! not open face recognize,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(true, "", "电子签名功能未启用！");
        }

        //获取用户所属租户的签名配置信息
        EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfig)
                || StringUtils.isBlank(eleEsignConfig.getAppId())
                || StringUtils.isBlank(eleEsignConfig.getAppSecret())
                || StringUtils.isBlank(eleEsignConfig.getDocTemplateId())) {
            log.error("ELE ERROR! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "000104", "租户电子签名配置信息不存在");
        }

        //检查用户是否已经完成签名的操作
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordMapper.selectEsignFinishedRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
        if(Objects.isNull(eleUserEsignRecord)){
            //检查用户是否存在最近却未被及时修改为完成状态的记录
            EleUserEsignRecord latestEsignRecord = eleUserEsignRecordMapper.selectLatestEsignRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
            if(Objects.nonNull(latestEsignRecord)){
                return checkStatusFromThirdParty(latestEsignRecord, eleEsignConfig);
            }

            //不存在，则直接返回未完成状态
            EleUserEsignRecord esignRecord = new EleUserEsignRecord();
            esignRecord.setUid(userInfo.getUid());
            esignRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
            esignRecord.setSignFinishStatus(EleEsignConstant.ESIGN_STATUS_FAILED);

            return Triple.of(true, "", esignRecord);
        }

        return Triple.of(true, "", eleUserEsignRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> checkStatusFromThirdParty(EleUserEsignRecord eleUserEsignRecord, EleEsignConfig eleEsignConfig){
        String signFlowId = eleUserEsignRecord.getSignFlowId();
        log.info("start check sign status from third party, signFlowId: {}", signFlowId);
        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        if(signFlowDetailResp.getData().getSignFlowStatus() == EleEsignConstant.ESIGN_FLOW_STATUS_COMPLETE){
            log.info("The signing status is not synchronized, updating the database record as completed, signFlowId: {}", signFlowId);
            eleUserEsignRecord.setSignFinishStatus(EleEsignConstant.ESIGN_STATUS_SUCCESS);
            eleUserEsignRecord.setUpdateTime(System.currentTimeMillis());
            eleUserEsignRecord.setSignResult(JsonUtil.toJson(signFlowDetailResp));
            eleUserEsignRecordMapper.updateUserEsignRecord(eleUserEsignRecord);
            return Triple.of(true, "", eleUserEsignRecord);
        }

        EleUserEsignRecord esignRecord = new EleUserEsignRecord();
        esignRecord.setUid(eleUserEsignRecord.getUid());
        esignRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
        esignRecord.setSignFinishStatus(EleEsignConstant.ESIGN_STATUS_FAILED);

        return Triple.of(true, "", esignRecord);
    }

    @Override
    public Triple<Boolean, String, Object> getSignatureFile(String signFlowId) {

        //获取用户所属租户的签名配置信息
        EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfig)
                || StringUtils.isBlank(eleEsignConfig.getAppId())
                || StringUtils.isBlank(eleEsignConfig.getAppSecret())) {
            log.error("ELE ERROR! esign config is null,tenantId={}",
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "000104", "租户电子签名配置信息不存在");
        }

        //查询签署流程状态，是否完成签署，如果完成，则将数据同步至数据库中，若未完成，则提示错误
        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());

        if(signFlowDetailResp.getData().getSignFlowStatus() == EleEsignConstant.ESIGN_FLOW_STATUS_COMPLETE){
            FileDownLoadResp fileDownLoadResp = signatureFileService.QueryDownLoadLink(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
            return Triple.of(true, "", fileDownLoadResp);
        }else{
            return Triple.of(false, "000108", "签署流程未完成！");
        }
    }

    /**
     * 添加用户成功认证后的记录
     * @param psnAuthDetailResp
     * @param uid
     */
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public void createUserIdentityAuthRecord(PsnAuthDetailResp psnAuthDetailResp, Long uid){
        if(Objects.nonNull(psnAuthDetailResp)){
            EleUserIdentityAuthRecord userIdentityAuthRecord = new EleUserIdentityAuthRecord();
            userIdentityAuthRecord.setUid(uid);
            userIdentityAuthRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
            userIdentityAuthRecord.setAuthFlowId(psnAuthDetailResp.getData().getAuthFlowId());

            PersonResp personResp = psnAuthDetailResp.getData().getAuthInfo().getPerson();
            userIdentityAuthRecord.setPsnId(personResp.getPsnId());
            userIdentityAuthRecord.setPsnAccount(personResp.getPsnAccount().getAccountMobile());
            userIdentityAuthRecord.setRealNameStatus(psnAuthDetailResp.getData().getRealNameStatus());
            userIdentityAuthRecord.setAuthResult(JsonUtil.toJson(psnAuthDetailResp));
            userIdentityAuthRecord.setDelFlag(EleEsignConstant.DEL_NO);
            userIdentityAuthRecord.setCreateTime(System.currentTimeMillis());
            userIdentityAuthRecord.setUpdateTime(System.currentTimeMillis());
            eleUserIdentityAuthRecordMapper.insertUserIdentityAuthRecord(userIdentityAuthRecord);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void createUserEsignRecord(Long uid, String signFlowId, String fileId, String fileName){
        EleUserEsignRecord eleUserEsignRecord = new EleUserEsignRecord();
        eleUserEsignRecord.setUid(uid);
        eleUserEsignRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
        eleUserEsignRecord.setSignFlowId(signFlowId);
        eleUserEsignRecord.setSignFinishStatus(EleEsignConstant.ESIGN_STATUS_FAILED);
        eleUserEsignRecord.setFileId(fileId);
        eleUserEsignRecord.setFileName(fileName);
        eleUserEsignRecord.setDelFlag(EleEsignConstant.DEL_NO);
        eleUserEsignRecord.setCreateTime(System.currentTimeMillis());
        eleUserEsignRecord.setUpdateTime(System.currentTimeMillis());
        eleUserEsignRecordMapper.insertUserEsignRecord(eleUserEsignRecord);
        //同时减除一次签名次数
        esignCapacityDataService.deductionCapacityByTenantId(TenantContextHolder.getTenantId().longValue());
    }

    @Override
    public void handleCallBackReq(Integer esignConfigId, HttpServletRequest request){

        boolean flag = false;
        String signature =  request.getHeader(EsignConstant.CALL_BACK_X_TSIGN_OPEN_SIGNATURE);
        //获取时间戳的字节流
        String timestamp = request.getHeader(EsignConstant.CALL_BACK_X_Tsign_Open_TIMESTAMP);

        log.info("Esign call back start, signature: {}, timestamp: {}", signature, timestamp);

        EleEsignConfig esignConfig = esignConfigMapper.selectEsignConfigById(esignConfigId);
        if(Objects.isNull(esignConfig)){
            log.error("Esign call back parameters error, esignConfigId: {}", esignConfigId);
            return;
        }
        //获取query请求字符串
        String requestQuery = SignUtils.getRequestQueryStr(request);
        //获取body的数据
        String reqBody =SignUtils.getRequestBody(request);
        log.info("The request of esign call back flow, requestQuery: {}, reqBody：{}", requestQuery, reqBody);
        //按照规则进行加密
        StringBuilder builder = new StringBuilder().append(timestamp).append(requestQuery).append(reqBody);
        String signData = builder.toString();
        String encryptionSignature = SignUtils.getSignature(signData, esignConfig.getAppSecret());
        log.info("The request of esign call back request body: {}", reqBody);
        if(encryptionSignature.equals(signature)) {
            EsignCallBackQuery esignCallBackQuery = JsonUtil.fromJson(reqBody, EsignCallBackQuery.class);
            log.info("Esign call back notice type is: {}", esignCallBackQuery.getAction());
            if(EsignConstant.CALL_BACK_ACTION_MISSON_COMPLETE.equals(esignCallBackQuery.getAction())){
                saveSignResultInfo(esignCallBackQuery.getSignFlowId(), esignConfig);
            }
        }else{
            log.error(" validate signature error for esign call back flow, signature: {}, encryptionSignature: {}", signature, encryptionSignature);
        }

    }
    @Transactional(rollbackFor = Exception.class)
    public void saveSignResultInfo(String signFlowId, EleEsignConfig esignConfig){
        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, esignConfig.getAppId(), esignConfig.getAppSecret());
        EleUserEsignRecord userEsignRecord = eleUserEsignRecordMapper.selectEsignRecordBySignFlowId(signFlowId);
        log.info("Esign call back flow ID：{}, user sign info: {}", signFlowId, userEsignRecord);
        if(Objects.nonNull(userEsignRecord)){
            log.info("update user esign record, user sign status: {}", signFlowDetailResp.getData().getSignFlowStatus());
            userEsignRecord.setSignFinishStatus(signFlowDetailResp.getData().getSignFlowStatus() == EleEsignConstant.ESIGN_FLOW_STATUS_COMPLETE ? EleEsignConstant.ESIGN_STATUS_SUCCESS : EleEsignConstant.ESIGN_STATUS_FAILED);
            userEsignRecord.setSignResult(JsonUtil.toJson(signFlowDetailResp));
            userEsignRecord.setUpdateTime(System.currentTimeMillis());
            eleUserEsignRecordMapper.updateUserEsignRecord(userEsignRecord);
        }
    }

}
