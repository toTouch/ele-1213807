package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.FaceAuthResultDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.FaceidResultQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.faceid.entity.dto.EidUserInfoDTO;
import com.xiliulou.faceid.entity.rsp.FaceidResultRsp;
import com.xiliulou.faceid.entity.rsp.FaceidTokenRsp;
import com.xiliulou.faceid.service.FaceidResultService;
import com.xiliulou.faceid.service.FaceidTokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-01-16:12
 */
@Slf4j
@Service
public class FaceidServiceImpl implements FaceidService {

    /**
     * 人脸核身最大透支次数
     */
    private static final Integer FACEID_MAX_OVERDRAFT_CAPACITY = -20;

    private Lock lock = new ReentrantLock();

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

    /**
     * 获取人脸核身token
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> getEidToken() {
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
        if (Objects.equals(ElectricityConfig.FACE_REVIEW, electricityConfig.getIsManualReview())) {
            log.error("ELE ERROR! not open face recognize,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "100337", "未开启人脸核身！");
        }


        //1.校验租户人脸核身资源包
        FaceRecognizeData faceRecognizeData = faceRecognizeDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faceRecognizeData)) {
            log.error("ELE ERROR! faceRecognizeData is null,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100334", "未购买人脸核身资源包，请联系管理员");
        }
        if (faceRecognizeData.getFaceRecognizeCapacity() < FACEID_MAX_OVERDRAFT_CAPACITY) {
            log.error("ELE ERROR! faceRecognizeData is null,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100335", "人脸核身资源包余额不足，请联系管理员");
        }

        //2.获取当前用户所属租户的商户号
        FaceidConfig faceidConfig = faceidConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faceidConfig) || StringUtils.isBlank(faceidConfig.getFaceMerchantId())) {
            log.error("ELE ERROR!faceidConfig is null,uid={},tenantId={}", SecurityUtils.getUid(), TenantContextHolder.getTenantId());
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
        if (Objects.equals(ElectricityConfig.FACE_REVIEW, electricityConfig.getIsManualReview())) {
            log.error("ELE ERROR! not open face recognize,tenantId={}", TenantContextHolder.getTenantId());
            return Triple.of(false, "100337", "未开启人脸核身！");
        }

        FaceidConfig faceidConfig = faceidConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(faceidConfig) || StringUtils.isBlank(faceidConfig.getFaceidPrivateKey())) {
            log.error("ELE ERROR!faceidConfig is null,uid={},tenantId={}", SecurityUtils.getUid(), TenantContextHolder.getTenantId());
            return Triple.of(false, "100332", "人脸核身配置信息不存在");
        }

        //保存人脸核身使用记录
        FaceRecognizeUserRecord faceRecognizeUserRecord = new FaceRecognizeUserRecord();
        faceRecognizeUserRecord.setStatus(faceidResultQuery.getVerifyDone() ? FaceRecognizeUserRecord.STATUS_SUCCESS : FaceRecognizeUserRecord.STATUS_FAIL);
        faceRecognizeUserRecord.setUid(userInfo.getUid());
        faceRecognizeUserRecord.setDelFlag(FaceRecognizeUserRecord.DEL_NORMAL);
        faceRecognizeUserRecord.setTenantId(TenantContextHolder.getTenantId());
        faceRecognizeUserRecord.setCreateTime(System.currentTimeMillis());
        faceRecognizeUserRecord.setUpdateTime(System.currentTimeMillis());
        FaceRecognizeUserRecord recognizeUserRecord = faceRecognizeUserRecordService.insert(faceRecognizeUserRecord);

        lock.lock();
        try {
            if (faceidResultQuery.getVerifyDone()) {
                return acquireEidResultAndDecode(faceidConfig, recognizeUserRecord, faceidResultQuery, userInfo);
            } else {
                //若人脸核身失败 更新用户实名认证状态及审核类型
                UserInfo userInfoUpdate = new UserInfo();
                userInfoUpdate.setId(userInfo.getId());
                userInfoUpdate.setAuthType(UserInfo.AUTH_TYPE_FACE);
                userInfoUpdate.setAuthStatus(UserInfo.AUTH_STATUS_FACE_FAIL);
                userInfoUpdate.setUpdateTime(System.currentTimeMillis());
                userInfoService.update(userInfoUpdate);
            }
        } finally {
            lock.unlock();
        }

        return Triple.of(false, "100331", "人脸核身失败");
    }

    /**
     * 获取人脸核身结果&解密
     */
    private Triple<Boolean, String, Object> acquireEidResultAndDecode(FaceidConfig faceidConfig, FaceRecognizeUserRecord recognizeUserRecord, FaceidResultQuery faceidResultQuery, UserInfo userInfo) {

        try {
            FaceidResultRsp faceidResultRsp = faceidResultService.acquireEidResult(faceidResultQuery.getToken());
            if (Objects.isNull(faceidResultRsp)) {
                log.error("ELE ERROR! faceidResultRsp is null,uid={}", userInfo.getUid());
                return Triple.of(false, "100330", "人脸核身结果获取失败");
            }

            //保存人脸核身结果
            FaceAuthResultData faceAuthResultData = faceAuthResultDataService.insert(buildFaceAuthResultData(faceidResultRsp));

            //更新人脸核身使用记录
            FaceRecognizeUserRecord recognizeUserRecordUpdate = new FaceRecognizeUserRecord();
            recognizeUserRecordUpdate.setId(recognizeUserRecord.getId());
            recognizeUserRecordUpdate.setAuthResultId(faceAuthResultData.getId());
            recognizeUserRecordUpdate.setUpdateTime(System.currentTimeMillis());
            faceRecognizeUserRecordService.update(recognizeUserRecordUpdate);


            //人脸核身结果解密
            EidUserInfoDTO eidUserInfo = faceidResultService.decodeEidResult(faceidResultRsp.getEidInfo(), faceidConfig.getFaceidPrivateKey());
            if (Objects.isNull(eidUserInfo)) {
                log.error("ELE ERROR! eidUserInfo is null,uid={}", userInfo.getUid());
                return Triple.of(false, "100330", "人脸核身结果获取失败");
            }

            //扣减人脸核身次数
            FaceRecognizeData faceRecognizeData = faceRecognizeDataService.selectByTenantId(TenantContextHolder.getTenantId());
            if (Objects.isNull(faceRecognizeData)) {
                log.error("ELE ERROR! faceRecognizeData is null,uid={}", SecurityUtils.getUid());
                return Triple.of(false, "100332", "人脸核身配置信息不存在");
            }

            FaceRecognizeData faceRecognizeDataUpdate = new FaceRecognizeData();
            faceRecognizeDataUpdate.setId(faceRecognizeData.getId());
            faceRecognizeDataUpdate.setFaceRecognizeCapacity(faceRecognizeData.getFaceRecognizeCapacity() - 1);
            faceRecognizeDataUpdate.setUpdateTime(System.currentTimeMillis());
            faceRecognizeDataService.updateById(faceRecognizeDataUpdate);

            //身份证号唯一性校验
            Integer idNumberExist = userInfoService.verifyIdNumberExist(eidUserInfo.getIdnum(), TenantContextHolder.getTenantId());
            if (!Objects.isNull(idNumberExist)) {
                log.error("ELE ERROR! idNumber already exist,uid={},idNumber={}", userInfo.getUid(), eidUserInfo.getIdnum());
                return Triple.of(false, "100339", "身份证号已存在");
            }

            //更新用户实名认证状态及审核类型
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            userInfoUpdate.setAuthType(UserInfo.AUTH_TYPE_FACE);
            userInfoUpdate.setIdNumber(eidUserInfo.getIdnum());
            userInfoUpdate.setName(eidUserInfo.getName());
            userInfoUpdate.setAuthStatus(UserInfo.AUTH_STATUS_REVIEW_PASSED);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.update(userInfoUpdate);

            return Triple.of(true, "", null);
        } catch (Exception e) {
            log.error("ELE ERROR! face recognize fail,uid={}", userInfo.getUid());
            return Triple.of(false, "100330", "人脸核身结果获取失败");
        }
    }


    private FaceAuthResultData buildFaceAuthResultData(FaceidResultRsp faceidResultRsp) {

        FaceAuthResultDTO faceAuthResultDTO = new FaceAuthResultDTO();
        faceAuthResultDTO.setEidDesKey(Objects.nonNull(faceidResultRsp.getEidInfo()) ? faceidResultRsp.getEidInfo().getDesKey() : "");
        faceAuthResultDTO.setEidCode(Objects.nonNull(faceidResultRsp.getEidInfo()) ? faceidResultRsp.getEidInfo().getEidCode() : "");
        faceAuthResultDTO.setEidSign(Objects.nonNull(faceidResultRsp.getEidInfo()) ? faceidResultRsp.getEidInfo().getEidSign() : "");
        faceAuthResultDTO.setEidUserInfo(Objects.nonNull(faceidResultRsp.getEidInfo()) ? faceidResultRsp.getEidInfo().getUserInfo() : "");
        faceAuthResultDTO.setOcrAddress(Objects.nonNull(faceidResultRsp.getTextInfo()) ? faceidResultRsp.getTextInfo().getOcrAddress() : "");
        faceAuthResultDTO.setOcrBirth(Objects.nonNull(faceidResultRsp.getTextInfo()) ? faceidResultRsp.getTextInfo().getOcrBirth() : "");
        faceAuthResultDTO.setOcrGender(Objects.nonNull(faceidResultRsp.getTextInfo()) ? faceidResultRsp.getTextInfo().getOcrGender() : "");
        faceAuthResultDTO.setOcrNation(Objects.nonNull(faceidResultRsp.getTextInfo()) ? faceidResultRsp.getTextInfo().getOcrNation() : "");

        FaceAuthResultData faceAuthResultData = new FaceAuthResultData();
        faceAuthResultData.setAuthResult(JsonUtil.toJson(faceAuthResultDTO));
        faceAuthResultData.setDelFlag(FaceAuthResultData.DEL_NORMAL);
        faceAuthResultData.setTenantId(TenantContextHolder.getTenantId());
        faceAuthResultData.setCreateTime(System.currentTimeMillis());
        faceAuthResultData.setUpdateTime(System.currentTimeMillis());

        return faceAuthResultData;
    }
}
