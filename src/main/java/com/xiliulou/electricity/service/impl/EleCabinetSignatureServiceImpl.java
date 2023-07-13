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
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.SignUtils;
import com.xiliulou.electricity.vo.SignFlowVO;
import com.xiliulou.esign.config.EsignConfig;
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
            log.error("获取个人认证链接异常!", e);
            return Triple.of(false, "000105", "签署流程获取用户认证信息失败！");
        }
        return Triple.of(true, "", psnAuthLinkResp.getData());
    }

    @Override
    public Triple<Boolean, String, Object> getSignFlowLink() {
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

        //根据模板id创建签署文件
        List<ComponentData> componentDataList = new ArrayList<>();
        FileCreateByTempResp fileCreateByTempResp = signatureFileService.createFileByTemplate(eleEsignConfig.getDocTemplateId(), eleEsignConfig.getSignFileName(),componentDataList, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        String fileId = fileCreateByTempResp.getData().getFileId();
        String fileName = eleEsignConfig.getSignFileName();

        //基于文件发起签署流程
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setPhone(userInfo.getPhone());
        userInfoQuery.setUserName(userInfo.getName());

        SignFlowDataQuery signFlowDataQuery = new SignFlowDataQuery();
        signFlowDataQuery.setFileId(fileId);
        signFlowDataQuery.setSignFileName(fileName);
        signFlowDataQuery.setSignFlowName(eleEsignConfig.getSignFlowName());
        signFlowDataQuery.setTenantAppId(eleEsignConfig.getAppId());
        signFlowDataQuery.setTenantAppSecret(eleEsignConfig.getAppSecret());
        signFlowDataQuery.setRedirectUrl(esignConfig.getRedirectUrlAfterSign());
        signFlowDataQuery.setNotifyUrl(esignConfig.getSignFlowNotifyUrl() + eleEsignConfig.getId());

        //根据模版ID获取组件位置
        SignComponentResp signComponentResp = signatureFileService.findComponentsLocation(eleEsignConfig.getDocTemplateId(),eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        ComponentPosition componentPosition = signComponentResp.getData().getComponents().get(0).getComponentPosition();

        signFlowDataQuery.setPositionPage(String.valueOf(componentPosition.getComponentPageNum()));
        signFlowDataQuery.setPositionX(componentPosition.getComponentPositionX());
        signFlowDataQuery.setPositionY(componentPosition.getComponentPositionY());
        SignDocsCreateResp signDocsCreateResp = electronicSignatureService.createByFileFlow(userInfoQuery, signFlowDataQuery);

        //根据signFlowId获取psnId信息，微信小程序跳转时需要该参数
        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signDocsCreateResp.getData().getSignFlowId(), eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());

        //获取文件签署链接
        SignFlowUrlResp signFlowUrlResp = electronicSignatureService.querySignFlowLink(signDocsCreateResp.getData().getSignFlowId(), userInfoQuery, signFlowDataQuery);
        String signFlowId = signDocsCreateResp.getData().getSignFlowId();

        SignFlowVO signFlowVO = new SignFlowVO();
        signFlowVO.setSignFlowId(signFlowId);
        signFlowVO.setPsnId(signFlowDetailResp.getData().getSigners().get(0).getPsnSigner().getPsnId());
        signFlowVO.setUrl(signFlowUrlResp.getData().getUrl());
        signFlowVO.setShortUrl(signFlowUrlResp.getData().getShortUrl());

        createUserEsignRecord(userInfo.getUid(), signFlowId, fileId, fileName);

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

        //获取用户所属租户的签名配置信息
        EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfig)
                || StringUtils.isBlank(eleEsignConfig.getAppId())
                || StringUtils.isBlank(eleEsignConfig.getAppSecret())) {
            log.error("ELE ERROR! esign config is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "000104", "租户电子签名配置信息不存在");
        }

        //检查用户是否已经完成签名的操作
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordMapper.selectLatestEsignRecordByUser(userInfo.getUid(), TenantContextHolder.getTenantId().longValue());
        if(Objects.isNull(eleUserEsignRecord)){
            EleUserEsignRecord esignRecord = new EleUserEsignRecord();
            esignRecord.setUid(userInfo.getUid());
            esignRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
            esignRecord.setSignFinishStatus(EleEsignConstant.ESIGN_STATUS_FAILED);

            return Triple.of(true, "", esignRecord);
        }

        if(eleUserEsignRecord.getSignFinishStatus() == EleEsignConstant.ESIGN_STATUS_SUCCESS){
            return Triple.of(true, "", eleUserEsignRecord);
        }

        return checkStatusFromThirdParty(eleUserEsignRecord, eleEsignConfig);

    }

    @Transactional(rollbackFor = Exception.class)
    private Triple<Boolean, String, Object> checkStatusFromThirdParty(EleUserEsignRecord eleUserEsignRecord, EleEsignConfig eleEsignConfig){
        String signFlowId = eleUserEsignRecord.getSignFlowId();
        log.info("开始从第三方验证签署状态， signFlowId: {}",signFlowId);
        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
        if(signFlowDetailResp.getData().getSignFlowStatus() == EleEsignConstant.ESIGN_FLOW_STATUS_COMPLETE){
            log.info("签署状态未同步，更新数据库记录为已完成。signFlowId: {}",signFlowId);
            eleUserEsignRecord.setSignFinishStatus(EleEsignConstant.ESIGN_STATUS_SUCCESS);
            eleUserEsignRecord.setUpdateTime(System.currentTimeMillis());
            eleUserEsignRecordMapper.updateUserEsignRecord(eleUserEsignRecord);
            //更新为成功完成状态后，减扣一次签署次数
            esignCapacityDataService.deductionCapacityByTenantId(TenantContextHolder.getTenantId().longValue());
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

        if(signFlowDetailResp.getData().getSignFlowStatus() == EleEsignConstant.ESIGN_FLOW_STATUS_COMPLETE){
            FileDownLoadResp fileDownLoadResp = signatureFileService.QueryDownLoadLink(signFlowId, eleEsignConfig.getAppId(), eleEsignConfig.getAppSecret());
            return Triple.of(true, "", fileDownLoadResp.getData());
        }else{
            return Triple.of(false, "000107", "签署流程未完成！");
        }
    }

    /**
     * 添加用户成功认证后的记录
     * @param psnAuthDetailResp
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    private void createUserIdentityAuthRecord(PsnAuthDetailResp psnAuthDetailResp, Long uid){
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
    private void createUserEsignRecord(Long uid, String signFlowId, String fileId, String fileName){
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
    }

    @Override
    public void handleCallBackReq(Integer esignConfigId, HttpServletRequest request){
        boolean flag = false;
        String signature =  request.getHeader("X-Tsign-Open-SIGNATURE");
        //获取时间戳的字节流
        String timestamp = request.getHeader("X-Tsign-Open-TIMESTAMP");

        EleEsignConfig esignConfig = esignConfigMapper.selectEsignConfigById(esignConfigId);
        if(Objects.isNull(esignConfig)){
            log.error("签名回调流程参数有错, esignConfigId : {}", esignConfigId);
            return;
        }
        //获取query请求字符串
        String requestQuery = SignUtils.getRequestQueryStr(request);
        //获取body的数据
        String reqBody =SignUtils.getRequestBody(request);
        //按照规则进行加密
        StringBuilder builder = new StringBuilder().append(timestamp).append(requestQuery).append(reqBody);
        String signData = builder.toString();
        String encryptionSignature = SignUtils.getSignature(signData, esignConfig.getAppSecret());
        log.info("签名回调请求信息, {}", reqBody);
        if(encryptionSignature.equals(signature)) {
            EsignCallBackQuery esignCallBackQuery = JsonUtil.fromJson(reqBody, EsignCallBackQuery.class);
            log.info("签名回调请求通知类型, {}", esignCallBackQuery.getAction());
            if(esignCallBackQuery.getAction().equals("SIGN_MISSON_COMPLETE")){
                saveSignResultInfo(esignCallBackQuery.getSignFlowId(), esignConfig);
            }
        }else{
            log.error("签名回调流程验签失败, signature : {}, encryptionSignature: ", signature, encryptionSignature);
        }

    }
    @Transactional(rollbackFor = Exception.class)
    private void saveSignResultInfo(String signFlowId, EleEsignConfig esignConfig){
        SignFlowDetailResp signFlowDetailResp = electronicSignatureService.querySignFlowDetailInfo(signFlowId, esignConfig.getAppId(), esignConfig.getAppSecret());
        EleUserEsignRecord userEsignRecord = eleUserEsignRecordMapper.selectEsignRecordBySignFlowId(signFlowId);
        log.info("签名回调流程ID：{}, 用户签署信息: {}", signFlowId, userEsignRecord);
        if(Objects.nonNull(userEsignRecord)){
            userEsignRecord.setSignFinishStatus(signFlowDetailResp.getData().getSignFlowStatus() == EleEsignConstant.ESIGN_FLOW_STATUS_COMPLETE ? EleEsignConstant.ESIGN_STATUS_SUCCESS : EleEsignConstant.ESIGN_STATUS_FAILED);
            userEsignRecord.setSignResult(JsonUtil.toJson(signFlowDetailResp));
            userEsignRecord.setUpdateTime(System.currentTimeMillis());
            eleUserEsignRecordMapper.updateUserEsignRecord(userEsignRecord);
            if(userEsignRecord.getSignFinishStatus() == EleEsignConstant.ESIGN_STATUS_SUCCESS){
                esignCapacityDataService.deductionCapacityByTenantId(esignConfig.getTenantId().longValue());
            }
        }
    }

}
