package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.dto.FaceAuthResultDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.query.FaceidResultQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.ImageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.faceid.entity.dto.EidUserInfoDTO;
import com.xiliulou.faceid.entity.rsp.FaceidResultRsp;
import com.xiliulou.faceid.entity.rsp.FaceidTokenRsp;
import com.xiliulou.faceid.service.FaceidResultService;
import com.xiliulou.faceid.service.FaceidTokenService;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import com.xiliulou.storage.service.impl.AliyunOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-01-16:12
 */
@Slf4j
@Service
public class FaceidServiceImpl implements FaceidService {

    private static final String OCR_OSS_PATH = "saas/idcard/";

    private static final String SUCCESS_MESSAGE = "成功";

    /**
     * 人脸核身最大透支次数
     */
    private static final Integer FACEID_MAX_OVERDRAFT_CAPACITY = -100;


    private static ExecutorService uploadIdcardPictureExecutor = XllThreadPoolExecutors.newFixedThreadPool("saveIdCardPicture",
            2, "SAVE_IDCARD_PICTURE");



    @Autowired
    private FaceidConfigService faceidConfigService;

    @Autowired
    private FaceidTokenService faceidTokenService;

    @Autowired
    private FaceidResultService faceidResultService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private FaceAuthResultDataService faceAuthResultDataService;

    @Autowired
    private FaceRecognizeDataService faceRecognizeDataService;

    @Autowired
    private FaceRecognizeUserRecordService faceRecognizeUserRecordService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    RedisService redisService;

    @Autowired
    StorageConfig storageConfig;
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;
    @Autowired
    AliyunOssService aliyunOssService;

    @Autowired
    private EleUserAuthService eleUserAuthService;

    /**
     * 获取人脸核身token
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> getEidToken() {

        if (!redisService
                .setNx(CacheConstant.ELE_CACHE_FACEID_TOKEN_LOCK_KEY + SecurityUtils.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE ERROR! user is unUsable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //用户是否已实名认证
        if (Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE ERROR! user already auth passed,uid={}", userInfo.getUid());
            return Triple.of(false, "100336", "用户已实名认证!");
        }

        //是否开启人脸核身
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR! electricityConfig is null,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "000001", "系统异常！");
        }
        if (!Objects.equals(ElectricityConfig.FACE_REVIEW, electricityConfig.getIsManualReview())) {
            log.error("ELE ERROR! not open face recognize,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "100337", "未开启人脸核身！");
        }

        //校验租户人脸核身资源包
        FaceRecognizeData faceRecognizeData = faceRecognizeDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faceRecognizeData)) {
            log.error("ELE ERROR! faceRecognizeData is null,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100334", "未购买人脸核身资源包，请联系管理员");
        }
        if (faceRecognizeData.getFaceRecognizeCapacity() <= FACEID_MAX_OVERDRAFT_CAPACITY) {
            log.error("ELE ERROR! faceRecognizeCapacity disable,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100335", "人脸核身资源包余额不足，请联系管理员");
        }

        //2.获取当前用户所属租户的商户号
        FaceidConfig faceidConfig = faceidConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faceidConfig) || StringUtils.isBlank(faceidConfig.getFaceMerchantId())) {
            log.error("ELE ERROR!faceidConfig is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "100332", "人脸核身配置信息不存在");
        }

        //3.获取人脸核身token
        try {
            FaceidTokenRsp faceidTokenRsp = faceidTokenService.acquireEidToken(faceidConfig.getFaceMerchantId());

            return Triple.of(true, "", faceidTokenRsp);
        } catch (Exception e) {
            log.error("ELE ERROR!acquire eidToken fail", e);
            return Triple.of(false, "100338", "获取人脸核身token失败！");
        }
    }

    /**
     * 获取人脸核身结果
     *
     * @return
     */
    @Override
    //    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> verifyEidResult(FaceidResultQuery faceidResultQuery) {

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FACEID_RESULT_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L,
                false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE ERROR! user is unUsable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //用户是否已实名认证
        if (Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE ERROR! user already auth passed,uid={}", userInfo.getUid());
            return Triple.of(false, "100336", "用户已实名认证!");
        }

        //是否开启人脸核身
        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR! electricityConfig is null,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "000001", "系统异常！");
        }
        if (!Objects.equals(ElectricityConfig.FACE_REVIEW, electricityConfig.getIsManualReview())) {
            log.error("ELE ERROR! not open face recognize,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "100337", "未开启人脸核身！");
        }

        FaceidConfig faceidConfig = faceidConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faceidConfig) || StringUtils.isBlank(faceidConfig.getFaceidPrivateKey())) {
            log.error("ELE ERROR!faceidConfig is null,uid={},tenantId={}", SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            return Triple.of(false, "100332", "人脸核身配置信息不存在");
        }

        //校验租户人脸核身资源包
        FaceRecognizeData faceRecognizeData = faceRecognizeDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faceRecognizeData)) {
            log.error("ELE ERROR! faceRecognizeData is null,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100334", "未购买人脸核身资源包，请联系管理员");
        }
        if (faceRecognizeData.getFaceRecognizeCapacity() <= FACEID_MAX_OVERDRAFT_CAPACITY) {
            log.error("ELE ERROR! faceRecognizeCapacity disable,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100335", "人脸核身资源包余额不足，请联系管理员");
        }

        //保存人脸核身使用记录
        FaceRecognizeUserRecord faceRecognizeUserRecord = new FaceRecognizeUserRecord();
        faceRecognizeUserRecord.setStatus(FaceRecognizeUserRecord.STATUS_FAIL);
        faceRecognizeUserRecord.setUid(userInfo.getUid());
        faceRecognizeUserRecord.setDelFlag(FaceRecognizeUserRecord.DEL_NORMAL);
        faceRecognizeUserRecord.setTenantId(TenantContextHolder.getTenantId());
        faceRecognizeUserRecord.setCreateTime(System.currentTimeMillis());
        faceRecognizeUserRecord.setUpdateTime(System.currentTimeMillis());
        FaceRecognizeUserRecord recognizeUserRecord = faceRecognizeUserRecordService.insert(faceRecognizeUserRecord);

        if (Boolean.TRUE.equals(faceidResultQuery.getVerifyDone())) {
            return acquireEidResultAndDecode(faceidConfig, recognizeUserRecord, faceidResultQuery, userInfo);
        } else {
            //若人脸核身失败 更新用户实名认证状态及审核类型
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setAuthType(UserInfo.AUTH_TYPE_FACE);
            userInfoUpdate.setAuthStatus(UserInfo.AUTH_STATUS_FACE_FAIL);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoUpdate.setTenantId(TenantContextHolder.getTenantId());
            userInfoService.update(userInfoUpdate);
        }


        return Triple.of(true, "", null);
    }

    /**
     * 获取人脸核身结果&解密
     */
    private Triple<Boolean, String, Object> acquireEidResultAndDecode(FaceidConfig faceidConfig,
                                                                      FaceRecognizeUserRecord recognizeUserRecord, FaceidResultQuery faceidResultQuery, UserInfo userInfo) {

        try {
            FaceidResultRsp faceidResultRsp = faceidResultService.acquireEidResult(faceidResultQuery.getToken());
            if (Objects.isNull(faceidResultRsp) || Objects.isNull(faceidResultRsp.getTextInfo()) || Objects.isNull(faceidResultRsp.getEidInfo())) {
                log.error("ELE ERROR! faceidResultRsp is null,uid={},resp={}", userInfo.getUid(), JsonUtil.toJson(faceidResultRsp));
                return Triple.of(false, "100330", "人脸核身结果获取失败");
            }

            //保存人脸核身结果
            FaceAuthResultData faceAuthResultData = faceAuthResultDataService.insert(buildFaceAuthResultData(faceidResultRsp));
            FaceRecognizeUserRecord recognizeUserRecordUpdate = new FaceRecognizeUserRecord();
            recognizeUserRecordUpdate.setId(recognizeUserRecord.getId());
            recognizeUserRecordUpdate.setAuthResultId(faceAuthResultData.getId());
            recognizeUserRecordUpdate.setUpdateTime(System.currentTimeMillis());
            faceRecognizeUserRecordService.update(recognizeUserRecordUpdate);

            if (!Objects.equals(faceidResultRsp.getTextInfo().getErrCode(), NumberConstant.ZERO_L) || !Objects.equals(faceidResultRsp.getTextInfo().getErrMsg(), SUCCESS_MESSAGE)) {
                log.error("ELE ERROR! face recognize fail,uid={},faceRecognizeRecordId={}", userInfo.getUid(), recognizeUserRecord.getId());
                return Triple.of(false, "100340", "人脸核身检测失败");
            }

            //更新人脸核身使用记录
            FaceRecognizeUserRecord faceRecognizeUserRecordUpdate = new FaceRecognizeUserRecord();
            faceRecognizeUserRecordUpdate.setId(recognizeUserRecord.getId());
            faceRecognizeUserRecordUpdate.setStatus(FaceRecognizeUserRecord.STATUS_SUCCESS);
            faceRecognizeUserRecordUpdate.setUpdateTime(System.currentTimeMillis());
            faceRecognizeUserRecordService.update(faceRecognizeUserRecordUpdate);

            //扣减人脸核身次数
            FaceRecognizeData faceRecognizeData = faceRecognizeDataService.selectByTenantId(TenantContextHolder.getTenantId());
            if (Objects.isNull(faceRecognizeData)) {
                log.error("ELE ERROR! faceRecognizeData is null,uid={}", SecurityUtils.getUid());
                return Triple.of(false, "100332", "人脸核身配置信息不存在");
            }

            FaceRecognizeData faceRecognizeDataUpdate = new FaceRecognizeData();
            faceRecognizeDataUpdate.setTenantId(TenantContextHolder.getTenantId());
            faceRecognizeDataUpdate.setUpdateTime(System.currentTimeMillis());
            faceRecognizeDataService.deductionCapacityByTenantId(faceRecognizeDataUpdate);

            //人脸核身结果解密
            EidUserInfoDTO eidUserInfo = faceidResultService.decodeEidResult(faceidResultRsp.getEidInfo(), faceidConfig.getFaceidPrivateKey());
            if (Objects.isNull(eidUserInfo)) {
                log.error("ELE ERROR! eidUserInfo is null,uid={}", userInfo.getUid());
                return Triple.of(false, "100341", "人脸核身结果解密失败，请联系管理员");
            }

            //身份证号唯一性校验
            Integer idNumberExist = userInfoService.verifyIdNumberExist(eidUserInfo.getIdnum(), TenantContextHolder.getTenantId());
            if (!Objects.isNull(idNumberExist)) {
                log.error("ELE ERROR! idNumber already exist,uid={},idNumber={}", userInfo.getUid(), eidUserInfo.getIdnum());
                return Triple.of(false, "100339", "身份证号已存在");
            }

            //更新用户实名认证状态及审核类型
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setAuthType(UserInfo.AUTH_TYPE_FACE);
            userInfoUpdate.setIdNumber(eidUserInfo.getIdnum());
            userInfoUpdate.setName(eidUserInfo.getName());
            userInfoUpdate.setAuthStatus(UserInfo.AUTH_STATUS_REVIEW_PASSED);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoUpdate.setTenantId(TenantContextHolder.getTenantId());
            userInfoService.update(userInfoUpdate);

            uploadIdcardInfo(userInfo,faceidResultRsp);

            return Triple.of(true, "", null);
        } catch (Exception e) {
            log.error("ELE ERROR! face recognize fail,uid={},query={}", userInfo.getUid(),
                    JsonUtil.toJson(faceidResultQuery), e);
            return Triple.of(false, "100330", "人脸核身失败");
        }finally {
            redisService.delete(CacheConstant.ELE_CACHE_FACEID_RESULT_LOCK_KEY + SecurityUtils.getUid());
        }
    }

    private void uploadIdcardInfo(UserInfo userInfo, FaceidResultRsp faceidResultRsp) {
        if (Objects.isNull(faceidResultRsp.getIdCardData()) || Objects.isNull(faceidResultRsp.getIdCardData().getOcrFront()) || Objects.isNull(faceidResultRsp.getIdCardData().getOcrBack())) {
            log.error("ELE ERROR! acquire user idcard picture error,uid={},result={}", userInfo.getUid(), JsonUtil.toJson(faceidResultRsp));
            return;
        }

        uploadIdcardPictureExecutor.execute(() -> {
            try {
                //身份证正面照片
                String ocrFrontPath = OCR_OSS_PATH + userInfo.getPhone() + userInfo.getUid() + "_front_" + userInfo.getUid() + ".png";

                byte[] ocrFrontBytes = ImageUtil.base64ToImage(faceidResultRsp.getIdCardData().getOcrFront());

                aliyunOssService.uploadFile(storageConfig.getBucketName(), ocrFrontPath, new ByteArrayInputStream(ocrFrontBytes));

                EleUserAuth userAuthFront = new EleUserAuth();
                userAuthFront.setUid(userInfo.getUid());
                userAuthFront.setEntryId(EleAuthEntry.ID_CARD_FRONT_PHOTO);
                userAuthFront.setValue(ocrFrontPath);
                userAuthFront.setCreateTime(System.currentTimeMillis());
                userAuthFront.setUpdateTime(System.currentTimeMillis());
                userAuthFront.setTenantId(userInfo.getTenantId());
                eleUserAuthService.insert(userAuthFront);

                //身份证反面照片
                String ocrBackPath = OCR_OSS_PATH + userInfo.getPhone() + userInfo.getUid() + "_back_" + userInfo.getUid() + ".png";

                byte[] ocrBackBytes = ImageUtil.base64ToImage(faceidResultRsp.getIdCardData().getOcrBack());

                aliyunOssService.uploadFile(storageConfig.getBucketName(), ocrBackPath, new ByteArrayInputStream(ocrBackBytes));

                EleUserAuth userAuthBack = new EleUserAuth();
                userAuthBack.setUid(userInfo.getUid());
                userAuthBack.setEntryId(EleAuthEntry.ID_CARD_BACK_PHOTO);
                userAuthBack.setValue(ocrFrontPath);
                userAuthBack.setCreateTime(System.currentTimeMillis());
                userAuthBack.setUpdateTime(System.currentTimeMillis());
                userAuthBack.setTenantId(userInfo.getTenantId());
                eleUserAuthService.insert(userAuthBack);
            } catch (Exception e) {
                log.error("ELE ERROR!upload idcard info fail,uid={},result={}", userInfo.getUid(), JsonUtil.toJson(faceidResultRsp));
            }
        });
    }


    private FaceAuthResultData buildFaceAuthResultData(FaceidResultRsp faceidResultRsp) {
        FaceAuthResultDTO faceAuthResultDTO = new FaceAuthResultDTO();

        faceAuthResultDTO.setEidDesKey(Objects.nonNull(faceidResultRsp.getEidInfo().getDesKey()) ? faceidResultRsp.getEidInfo().getDesKey() : "");
        faceAuthResultDTO.setEidCode(Objects.nonNull(faceidResultRsp.getEidInfo().getEidCode()) ? faceidResultRsp.getEidInfo().getEidCode() : "");
        faceAuthResultDTO.setEidSign(Objects.nonNull(faceidResultRsp.getEidInfo().getEidSign()) ? faceidResultRsp.getEidInfo().getEidSign() : "");
        faceAuthResultDTO.setEidUserInfo(Objects.nonNull(faceidResultRsp.getEidInfo().getUserInfo()) ? faceidResultRsp.getEidInfo().getUserInfo() : "");
        faceAuthResultDTO.setOcrAddress(Objects.nonNull(faceidResultRsp.getTextInfo().getOcrAddress()) ? faceidResultRsp.getTextInfo().getOcrAddress() : "");
        faceAuthResultDTO.setOcrBirth(Objects.nonNull(faceidResultRsp.getTextInfo().getOcrBirth()) ? faceidResultRsp.getTextInfo().getOcrBirth() : "");
        faceAuthResultDTO.setOcrGender(Objects.nonNull(faceidResultRsp.getTextInfo().getOcrGender()) ? faceidResultRsp.getTextInfo().getOcrGender() : "");
        faceAuthResultDTO.setOcrNation(Objects.nonNull(faceidResultRsp.getTextInfo().getOcrNation()) ? faceidResultRsp.getTextInfo().getOcrNation() : "");
        faceAuthResultDTO.setErrCode(Objects.nonNull(faceidResultRsp.getTextInfo().getErrCode()) ? faceidResultRsp.getTextInfo().getErrCode() : -1);
        faceAuthResultDTO.setErrMsg(Objects.nonNull(faceidResultRsp.getTextInfo().getErrMsg()) ? faceidResultRsp.getTextInfo().getErrMsg() : "");
        faceAuthResultDTO.setLiveStatus(Objects.nonNull(faceidResultRsp.getTextInfo().getLiveStatus()) ? faceidResultRsp.getTextInfo().getLiveStatus() : -1);
        faceAuthResultDTO.setLiveMsg(Objects.nonNull(faceidResultRsp.getTextInfo().getLiveMsg()) ? faceidResultRsp.getTextInfo().getLiveMsg() : "");

        FaceAuthResultData faceAuthResultData = new FaceAuthResultData();
        faceAuthResultData.setAuthResult(JsonUtil.toJson(faceAuthResultDTO));
        faceAuthResultData.setDelFlag(FaceAuthResultData.DEL_NORMAL);
        faceAuthResultData.setTenantId(TenantContextHolder.getTenantId());
        faceAuthResultData.setCreateTime(System.currentTimeMillis());
        faceAuthResultData.setUpdateTime(System.currentTimeMillis());

        return faceAuthResultData;
    }
}
