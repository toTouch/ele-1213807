package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.AuthenticationAuditMessageNotify;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.UserAuthMessage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.EleUserAuthMapper;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.IdCardCheckService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.UserAuthMessageService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserAuthMessageVO;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 实名认证信息(TEleUserAuth)表服务实现类
 *
 * @author makejava
 * @since 2021-02-20 13:37:
 */
@Service("eleUserAuthService")
@Slf4j
public class EleUserAuthServiceImpl implements EleUserAuthService {
    @Resource
    EleUserAuthMapper eleUserAuthMapper;

    @Autowired
    EleAuthEntryService eleAuthEntryService;

    @Autowired
    UserInfoService userInfoService;

    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;

    @Autowired
    StorageConfig storageConfig;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    RocketMqService rocketMqService;

    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;

    @Autowired
    UserAuthMessageService userAuthMessageService;
    
    @Resource
    private IdCardCheckService idCardCheckService;
    

    /**
     * 新增数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleUserAuth insert(EleUserAuth eleUserAuth) {
        this.eleUserAuthMapper.insert(eleUserAuth);
        return eleUserAuth;
    }
    
    @Override
    public Integer batchInsert(List<EleUserAuth> list) {
        return this.eleUserAuthMapper.batchInsert(list);
    }
    
    /**
     * 修改数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleUserAuth eleUserAuth) {
        return this.eleUserAuthMapper.updateById(eleUserAuth);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R webAuth(List<EleUserAuth> eleUserAuthList) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("payDeposit  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        UserInfo oldUserInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(oldUserInfo)) {
            log.warn("ELECTRICITY WARN! not found user！uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("ELECTRICITY WARN! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (Objects.equals(oldUserInfo.getAuthStatus(), UserInfo.AUTH_STATUS_PENDING_REVIEW)) {
            return R.fail("审核中，无法修改!");
        }

        if (Objects.equals(oldUserInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return R.fail("审核通过，无法修改!");
        }
    
        Triple<Boolean, String, Object> checkResult = checkIdCard(tenantId,eleUserAuthList);
        if (!checkResult.getLeft()) {
            return R.fail(ObjectUtils.isEmpty(checkResult.getRight()) ? null : checkResult.getRight().toString(), checkResult.getMiddle());
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(oldUserInfo.getId());

        //是否需要人工审核
        Integer status = EleUserAuth.STATUS_PENDING_REVIEW;
    
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig)) {
            log.warn("not found electricityConfig,uid={}", user.getUid());
            return R.fail("系统配置不存在");
        }
        
        if (Objects.equals(electricityConfig.getIsManualReview(), ElectricityConfig.AUTO_REVIEW)) {
            status = EleUserAuth.STATUS_REVIEW_PASSED;
            userInfo.setAuthType(UserInfo.AUTH_TYPE_SYSTEM);
        }

        for (EleUserAuth eleUserAuth : eleUserAuthList) {
            eleUserAuth.setUid(user.getUid());

            EleAuthEntry eleAuthEntryDb = eleAuthEntryService.queryByIdFromCache(eleUserAuth.getEntryId());
            if (Objects.isNull(eleAuthEntryDb)) {
                log.error("not found authEntry entryId:{}", eleUserAuth.getEntryId());
                return R.fail("审核资料项不存在!");
            }

            if (ObjectUtil.equal(EleAuthEntry.ID_NAME_ID, eleUserAuth.getEntryId())) {
                userInfo.setName(eleUserAuth.getValue());
            }
            if (ObjectUtil.equal(EleAuthEntry.ID_ID_CARD, eleUserAuth.getEntryId())) {
                userInfo.setIdNumber(eleUserAuth.getValue());
            }

            eleUserAuth.setStatus(status);
            eleUserAuth.setUpdateTime(System.currentTimeMillis());
            if (Objects.isNull(eleUserAuth.getId())) {
                eleUserAuth.setCreateTime(System.currentTimeMillis());
                eleUserAuth.setTenantId(tenantId);
                eleUserAuthMapper.insert(eleUserAuth);
            } else {
                eleUserAuthMapper.updateById(eleUserAuth);
            }
        }

        userInfo.setUid(user.getUid());
        userInfo.setAuthType(transfomStatus(electricityConfig.getIsManualReview()));
        userInfo.setAuthStatus(status);
        userInfo.setTenantId(tenantId);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer result = userInfoService.update(userInfo);
    
        boolean flag = result > 0 && Objects.equals(electricityConfig.getIsManualReview(), ElectricityConfig.MANUAL_REVIEW);
        if (flag) {
            sendAuthenticationAuditMessage(userInfo);
        }

        return R.ok();
    }
    
    private Integer transfomStatus(Integer isManualReview) {
        Integer result = ElectricityConfig.MANUAL_REVIEW;
        switch (isManualReview) {
            case 0:
                result = UserInfo.AUTH_TYPE_PERSON;
                break;
            case 1:
                result = UserInfo.AUTH_TYPE_SYSTEM;
                break;
            case 2:
                result = UserInfo.AUTH_TYPE_FACE;
                break;
        }
        return result;
    }
    
    
    private Triple<Boolean, String, Object> checkIdCard(Integer tenantId, List<EleUserAuth> eleUserAuthList) {
        if (CollectionUtils.isEmpty(eleUserAuthList)) {
            return Triple.of(false, "资料项为空", null);
        }
        String idCard= null;
        for (EleUserAuth eleUserAuth : eleUserAuthList) {
            if (!ObjectUtil.equal(EleAuthEntry.ID_ID_CARD, eleUserAuth.getEntryId())) {
                continue;
            }
            idCard = eleUserAuth.getValue();
            List<UserInfo> userInfos = userInfoService.queryByIdNumber(eleUserAuth.getValue());
            if (CollectionUtils.isEmpty(userInfos)) {
                break;
            }
            
            for (UserInfo userInfo : userInfos) {
                if (!Objects.equals(SecurityUtils.getUid(), userInfo.getUid())) {
                    return Triple.of(false, "身份证信息已存在，请核实后重新提交", 100339);
                }
            }
            break;
        }
        // 校验身份证是否符合年龄
        return idCardCheckService.checkIdNumber(tenantId, idCard);
    }
    
    private void sendAuthenticationAuditMessage(UserInfo userInfo) {
        List<MqNotifyCommon<AuthenticationAuditMessageNotify>> messageNotifyList = this.buildAuthenticationAuditMessageNotify(userInfo);
        if (CollectionUtils.isEmpty(messageNotifyList)) {
            return;
        }
    
        messageNotifyList.forEach(i -> {
            rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(i), "", "", 0);
        });
    }
    
    private List<MqNotifyCommon<AuthenticationAuditMessageNotify>> buildAuthenticationAuditMessageNotify(UserInfo userInfo) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(userInfo.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.warn("ELE WARN! not found maintenanceUserNotifyConfig,tenantId={},uid={}", userInfo.getTenantId(),userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
    
        if ((notifyConfig.getPermissions() & MaintenanceUserNotifyConfig.P_AUTHENTICATION_AUDIT)
                != MaintenanceUserNotifyConfig.P_AUTHENTICATION_AUDIT) {
            log.info("ELE INFO! not maintenance permission,permissions={},uid={}", notifyConfig.getPermissions(),userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
        
        
        List<String> phones = JSON.parseObject(notifyConfig.getPhones(), List.class);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(phones)) {
            log.warn("ELE WARN! phones is empty,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
        
        return phones.parallelStream().map(item -> {
            AuthenticationAuditMessageNotify messageNotify = new AuthenticationAuditMessageNotify();
            messageNotify.setBusinessCode(StringUtils.isBlank(userInfo.getIdNumber()) ? "/" : userInfo.getIdNumber().substring(userInfo.getIdNumber().length() - 6));
            messageNotify.setUserName(userInfo.getName());
            messageNotify.setAuthTime(DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
            
            MqNotifyCommon<AuthenticationAuditMessageNotify> authMessageNotifyCommon = new MqNotifyCommon<>();
            authMessageNotifyCommon.setTime(System.currentTimeMillis());
            authMessageNotifyCommon.setType(MqNotifyCommon.TYPE_AUTHENTICATION_AUDIT);
            authMessageNotifyCommon.setPhone(item);
            authMessageNotifyCommon.setData(messageNotify);
            return authMessageNotifyCommon;
        }).collect(Collectors.toList());
    }
    
    @Override
    public R getEleUserAuthSpecificStatus(Long uid) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELECTRICITY  WARN! not found userInfo! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(userInfo.getAuthStatus());
    }

    @Override
    public R selectUserAuthStatus(Long uid) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELE WARN! not found userInfo! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserAuthMessageVO userAuthMessageVO = new UserAuthMessageVO();
        userAuthMessageVO.setUid(userInfo.getUid());
        userAuthMessageVO.setAuthStatus(userInfo.getAuthStatus());

        if(Objects.equals(userInfo.getAuthStatus(),UserInfo.AUTH_STATUS_REVIEW_REJECTED)){
            UserAuthMessage userAuthMessage = userAuthMessageService.selectLatestByUid(uid);
            userAuthMessageVO.setMsg(Objects.isNull(userAuthMessage)?"":userAuthMessage.getMsg());
        }

        return R.ok(userAuthMessageVO);
    }

    @Override
    public R selectCurrentEleAuthEntriesList(Long uid) {
        List<EleUserAuth> eleUserAuths = eleUserAuthMapper.selectList(Wrappers.<EleUserAuth>lambdaQuery().eq(EleUserAuth::getUid, uid).eq(EleUserAuth::getDelFlag, EleUserAuth.DEL_NORMAL));
        if (!DataUtil.collectionIsUsable(eleUserAuths)) {
            return R.ok(Collections.emptyList());
        }

        List<EleUserAuth> collect = eleUserAuths.stream().map(e -> {
            if (e.getEntryId().equals(EleAuthEntry.ID_CARD_BACK_PHOTO) || e.getEntryId().equals(EleAuthEntry.ID_CARD_FRONT_PHOTO) || e.getEntryId().equals(EleAuthEntry.ID_SELF_PHOTO)) {
                if (StringUtils.isNotEmpty(e.getValue())) {
                    e.setValue("https://" + storageConfig.getUrlPrefix() + "/" + e.getValue());
                }

            }
            return e;
        }).collect(Collectors.toList());
        log.info("collect is -->{}", collect);
        return R.ok(collect);
    }

    @Override
    public void updateByUid(Long uid, Integer authStatus) {
        eleUserAuthMapper.updateByUid(uid, authStatus, System.currentTimeMillis());
    }

    @Override
    public EleUserAuth queryByUidAndEntryId(Long uid, Integer idIdCard) {
        return eleUserAuthMapper.selectOne(Wrappers.<EleUserAuth>lambdaQuery().eq(EleUserAuth::getUid, uid).eq(EleUserAuth::getEntryId, idIdCard).eq(EleUserAuth::getDelFlag, EleUserAuth.DEL_NORMAL));
    }

    @Override
    public R acquireIdcardFileSign() {
        return R.ok(storageService.getOssUploadSign("idcard/"));
    }

    @Override
    public R acquireselfieFileSign() {
        return R.ok(storageService.getOssUploadSign("selfie/"));
    }
}
