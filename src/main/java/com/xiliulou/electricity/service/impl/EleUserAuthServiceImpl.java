package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.MqConstant;
import com.xiliulou.electricity.entity.AuthenticationAuditMessageNotify;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.EleUserAuthMapper;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
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
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    RocketMqService rocketMqService;
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;

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
            log.error("ELECTRICITY  ERROR! not found user！uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (Objects.equals(oldUserInfo.getAuthStatus(), UserInfo.AUTH_STATUS_PENDING_REVIEW)) {
            return R.fail("审核中，无法修改!");
        }

        if (Objects.equals(oldUserInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return R.fail("审核通过，无法修改!");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(oldUserInfo.getId());

        //是否需要人工审核
        Integer status = EleUserAuth.STATUS_PENDING_REVIEW;
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.nonNull(electricityConfig)) {
            if (Objects.equals(electricityConfig.getIsManualReview(), ElectricityConfig.AUTO_REVIEW)) {
                status = EleUserAuth.STATUS_REVIEW_PASSED;
                userInfo.setServiceStatus(UserInfo.STATUS_IS_AUTH);
            }
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
        userInfo.setAuthStatus(status);
        userInfo.setTenantId(tenantId);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer result = userInfoService.update(userInfo);
    
        boolean flag = result > 0 && Objects.nonNull(electricityConfig)
                && Objects.equals(electricityConfig.getIsManualReview(), ElectricityConfig.MANUAL_REVIEW);
        
        if (flag) {
            sendAuthenticationAuditMessage(userInfo);
        }
    
        return R.ok();
    }
    
    private void sendAuthenticationAuditMessage(UserInfo userInfo) {
        List<MqNotifyCommon<AuthenticationAuditMessageNotify>> messageNotifyList = this.buildAuthenticationAuditMessageNotify(userInfo);
        if (CollectionUtils.isEmpty(messageNotifyList)) {
            return;
        }
    
        messageNotifyList.forEach(i -> {
            rocketMqService.sendAsyncMsg(MqConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(i), "", "", 0);
        });
    }
    
    private List<MqNotifyCommon<AuthenticationAuditMessageNotify>> buildAuthenticationAuditMessageNotify(UserInfo userInfo) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(userInfo.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.error("ELE ERROR! not found maintenanceUserNotifyConfig,tenantId={},uid={}", userInfo.getTenantId(),userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
    
        if ((notifyConfig.getPermissions() & MaintenanceUserNotifyConfig.P_AUTHENTICATION_AUDIT)
                != MaintenanceUserNotifyConfig.P_AUTHENTICATION_AUDIT) {
            log.info("ELE ERROR! not maintenance permission,permissions={},uid={}", notifyConfig.getPermissions(),userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
        
        
        List<String> phones = JSON.parseObject(notifyConfig.getPhones(), List.class);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(phones)) {
            log.error("ELE ERROR! phones is empty,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
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
            log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(userInfo.getAuthStatus());
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
    public R getEleUserServiceStatus() {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("payDeposit  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        Integer serviceStatus = userInfo.getServiceStatus();

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Long now = System.currentTimeMillis();
        if (!Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)) {
            serviceStatus = franchiseeUserInfo.getServiceStatus();
        }

//        //用户是否开通月卡
//        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
//                || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
//            log.error("order  ERROR! not found memberCard ! uid:{} ", user.getUid());
//            serviceStatus = -1;
//        } else {
//            if (franchiseeUserInfo.getMemberCardExpireTime() < now || franchiseeUserInfo.getRemainingNumber() == 0) {
//                log.error("order  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
//                serviceStatus = -1;
//            }
//        }

        return R.ok(serviceStatus);
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
