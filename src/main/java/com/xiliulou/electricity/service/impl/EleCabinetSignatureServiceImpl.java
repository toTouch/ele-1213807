package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.EleEsignConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleUserEsignRecordMapper;
import com.xiliulou.electricity.mapper.EleUserIdentityAuthRecordMapper;
import com.xiliulou.electricity.service.EleCabinetSignatureService;
import com.xiliulou.electricity.service.EleEsignConfigService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.SignFlowVO;
import com.xiliulou.esign.config.EsignConfig;
import com.xiliulou.esign.entity.query.ComponentData;
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
    private EsignConfig esignConfig;
    @Autowired
    private EleUserIdentityAuthRecordMapper eleUserIdentityAuthRecordMapper;
    @Autowired
    private EleUserEsignRecordMapper eleUserEsignRecordMapper;

    private static ExecutorService savePsnAuthResultExecutor = XllThreadPoolExecutors.newFixedThreadPool("savePsnAuthResult",
            2, "SAVE_PSN_AUTH_RESULT");

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
            log.error("获取个人认证链接异常!", e);
            return Triple.of(false, "000105", "签署流程获取用户认证信息失败！");
        }
        return Triple.of(true, "", psnAuthLinkResp.getData());
    }

    @Override
    public Triple<Boolean, String, Object> fileSignatureFlow(String authFlowId) {

        //获取当前用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "000100", "未找到用户");
        }

        //获取用户所属租户的签名配置信息
        EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfig)
                || StringUtils.isBlank(eleEsignConfig.getAppId())
                || StringUtils.isBlank(eleEsignConfig.getAppSecret())) {
            log.error("ELE ERROR! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "000104", "租户电子签名配置信息不存在");
        }

        //查看用户认证详细信息，并检查当前用户是否进行过身份认证。如果已经认证过则将用户的认证信息进行保存。保存之前需要判断是否已经记录过。
        PsnAuthDetailResp psnAuthDetailResp = personalAuthenticationService.queryPsnDetailInfo(authFlowId, esignConfig.getXxlAppId(), esignConfig.getXxlAppSecret());
        if(psnAuthDetailResp.getCode() == 0 && psnAuthDetailResp.getData().getRealNameStatus() == 1){
            //认证已通过，开始签署的流程。同时将拿到认证的个人信息，保存在数据库中。异步保存。
            //先判断当前库中是否存在这个用户的记录
            EleUserIdentityAuthRecord eleUserIdentityAuthRecord = eleUserIdentityAuthRecordMapper.selectLatestAuthRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
            if(Objects.isNull(eleUserIdentityAuthRecord)){
                //保存认证结果数据
                savePsnAuthResult(userInfo, psnAuthDetailResp);
            }
        }else{
            return Triple.of(false, "000106", "未完成实名认证，请先完成！");
        }

        //判断之前是否已经签署过，查询签署的状态，若签署过，则直接返回，若没有签署过，则继续下面的流程
        //先查询本地库中是否已经存在签署记录，若存在则已经签署过
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordMapper.selectLatestEsignRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
        if(Objects.nonNull(eleUserEsignRecord) && eleUserEsignRecord.getSignFinishStatus() == 1){
            SignFlowVO signFlowVO = new SignFlowVO();
            signFlowVO.setSignFlowId(eleUserEsignRecord.getSignFlowId());
            return Triple.of(true, "", signFlowVO);
        }

        //根据模板id创建签署文件
        List<ComponentData> componentDataList = new ArrayList<>();
        FileCreateByTempResp fileCreateByTempResp = signatureFileService.createFileByTemplate(eleEsignConfig.getDocTemplateId(), eleEsignConfig.getSignFileName(),componentDataList, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        String fileId = fileCreateByTempResp.getData().getFileId();

        //基于文件发起签署流程
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setPhone(userInfo.getPhone());
        userInfoQuery.setUserName(userInfo.getName());

        SignFlowDataQuery signFlowDataQuery = new SignFlowDataQuery();
        signFlowDataQuery.setFileId(fileId);
        signFlowDataQuery.setSignFileName(eleEsignConfig.getSignFileName());
        signFlowDataQuery.setSignFlowName(eleEsignConfig.getSignFlowName());
        signFlowDataQuery.setTenantAppId(eleEsignConfig.getAppId());
        signFlowDataQuery.setTenantAppSecret(eleEsignConfig.getAppSecret());
        signFlowDataQuery.setRedirectUrl(esignConfig.getRedirectUrlAfterSign());

        SignDocsCreateResp signDocsCreateResp = electronicSignatureService.createByFileFlow(userInfoQuery, signFlowDataQuery);

        //获取文件签署链接
        SignFlowUrlResp signFlowUrlResp = electronicSignatureService.querySignFlowLink(signDocsCreateResp.getData().getSignFlowId(), userInfoQuery, signFlowDataQuery);

        SignFlowVO signFlowVO = new SignFlowVO();
        signFlowVO.setSignFlowId(signDocsCreateResp.getData().getSignFlowId());
        signFlowVO.setUrl(signFlowUrlResp.getData().getUrl());
        signFlowVO.setShortUrl(signFlowUrlResp.getData().getShortUrl());

        return Triple.of(true, "", signFlowVO);
    }

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

        //检查用户是否已经完成签名的操作
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordMapper.selectLatestEsignRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
        if(Objects.nonNull(eleUserEsignRecord) && eleUserEsignRecord.getSignFinishStatus() == 1){
            log.info("user esign finished: {}", eleUserEsignRecord);
            return Triple.of(true, "", eleUserEsignRecord);
        }

        return Triple.of(false, "", "用户签署未完成！");
    }

    @Override
    public Triple<Boolean, String, Object> getSignatureFile(String signFlowId) {
        //获取当前用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "000100", "未找到用户");
        }

        //获取用户所属租户的签名配置信息
        EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfig)
                || StringUtils.isBlank(eleEsignConfig.getAppId())
                || StringUtils.isBlank(eleEsignConfig.getAppSecret())) {
            log.error("ELE ERROR! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "000104", "租户电子签名配置信息不存在");
        }

        //查询签署流程状态，是否完成签署，如果完成，则将数据同步至数据库中，若未完成，则提示错误
        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        if(Objects.nonNull(signFlowDetailResp) && signFlowDetailResp.getData().getSignFlowStatus() == 2){
            //检查当前库中是否已经存在该用户的签署记录
            EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordMapper.selectLatestEsignRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
            if(Objects.isNull(eleUserEsignRecord)){
                createUserEsignRecord(signFlowDetailResp, userInfo.getUid(), signFlowId);
            }
        }else{
            return Triple.of(false, "000107", "签署流程未完成！");
        }

        FileDownLoadResp fileDownLoadResp = signatureFileService.QueryDownLoadLink(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        return Triple.of(true, "", fileDownLoadResp.getData());
    }

    /**
     * 添加用户成功认证后的记录
     * @param psnAuthDetailResp
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    private void createUserIdentityAuthRecord(PsnAuthDetailResp psnAuthDetailResp, Long uid){
        EleUserIdentityAuthRecord userIdentityAuthRecord = new EleUserIdentityAuthRecord();
        userIdentityAuthRecord.setUid(uid);
        userIdentityAuthRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
        userIdentityAuthRecord.setAuthFlowId(psnAuthDetailResp.getData().getAuthFlowId());

        PersonResp personResp = psnAuthDetailResp.getData().getAuthInfo().getPerson();
        userIdentityAuthRecord.setPsnId(personResp.getPsnId());
        userIdentityAuthRecord.setPsnAccount(personResp.getPsnAccount().getAccountMobile());
        userIdentityAuthRecord.setRealNameStatus(psnAuthDetailResp.getData().getRealNameStatus());
        userIdentityAuthRecord.setDelFlag(EleEsignConstant.DEL_NO);
        userIdentityAuthRecord.setCreateTime(System.currentTimeMillis());
        userIdentityAuthRecord.setUpdateTime(System.currentTimeMillis());
        eleUserIdentityAuthRecordMapper.insertUserIdentityAuthRecord(userIdentityAuthRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    private void updateUserIdentityAuthRecord(EleUserIdentityAuthRecord eleUserIdentityAuthRecord, int realNameStatus){
        eleUserIdentityAuthRecord.setRealNameStatus(realNameStatus);
        eleUserIdentityAuthRecordMapper.updateUserIdentityAuthRecord(eleUserIdentityAuthRecord);
    }

    /**
     * 添加用户
     * @param signFlowDetailResp
     * @param uid
     * @param signFlowId
     */
    @Transactional(rollbackFor = Exception.class)
    private void createUserEsignRecord(SignFlowDetailResp signFlowDetailResp, Long uid, String signFlowId){
        EleUserEsignRecord eleUserEsignRecord = new EleUserEsignRecord();
        eleUserEsignRecord.setUid(uid);
        eleUserEsignRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
        eleUserEsignRecord.setSignFlowId(signFlowId);
        eleUserEsignRecord.setSignFinishStatus(signFlowDetailResp.getData().getSignFlowStatus() == 2 ? 1 : 0);
        //当前签署文件只有一个，则取第一个文档的信息即可
        SignDocData signDocData = signFlowDetailResp.getData().getDocs().get(0);
        eleUserEsignRecord.setFileId(signDocData.getFileId());
        eleUserEsignRecord.setFileName(signDocData.getFileName());
        eleUserEsignRecord.setDelFlag(EleEsignConstant.DEL_NO);
        eleUserEsignRecord.setCreateTime(System.currentTimeMillis());
        eleUserEsignRecord.setUpdateTime(System.currentTimeMillis());
        eleUserEsignRecordMapper.insertUserEsignRecord(eleUserEsignRecord);
    }


}
