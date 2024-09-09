package com.xiliulou.electricity.service.impl.installment;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.message.RechargeAlarm;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.event.publish.SiteMessagePublish;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.query.installment.HandleTerminatingRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQueryAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQuerySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyReleaseAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.Vars;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyAgreementPayRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQueryAgreementPayRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQuerySignAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyReleaseAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyResult;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FySignAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.service.FyAgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_AGREEMENT_PAY_NOTIFY_LOCK;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_CANCEL_SIGN;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_DEDUCT_LOCK;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_FORM_BODY;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_SIGN_NOTIFY_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.AGREEMENT_PAY_QUERY_STATUS_SUCCESS;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.CHANNEL_FROM_H5;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_CANCEL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_PAID;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_SUCCESS;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.FY_SUCCESS_CODE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_CANCELLED;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_COMPLETED;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_TERMINATE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UN_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PACKAGE_TYPE_BATTERY;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.SIGN_QUERY_STATUS_CANCEL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.SIGN_QUERY_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_REFUSE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_RELEASE;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/7 22:20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstallmentBizServiceImpl implements InstallmentBizService {
    
    private final InstallmentRecordService installmentRecordService;
    
    private final InstallmentDeductionPlanService installmentDeductionPlanService;
    
    private final InstallmentDeductionRecordService installmentDeductionRecordService;
    
    private final InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    private final FyConfigService fyConfigService;
    
    private final FyAgreementService fyAgreementService;
    
    private final RedisService redisService;
    
    private final UserInfoService userInfoService;
    
    private final FengYunConfig fengYunConfig;
    
    private final BatteryMemberCardService batteryMemberCardService;
    
    private final UserBatteryMemberCardService userBatteryMemberCardService;
    
    private final ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    private final UnionTradeOrderService unionTradeOrderService;
    
    private final TenantService tenantService;
    
    private final EleRefundOrderService eleRefundOrderService;
    
    private final SiteMessagePublish siteMessagePublish;
    
    private final FreeDepositDataService freeDepositDataService;
    
    XllThreadPoolExecutorService initiatingDeductThreadPool;
    
    @PostConstruct
    public void init() {
        initiatingDeductThreadPool = XllThreadPoolExecutors.newFixedThreadPool("INSTALLMENT_INITIATING_DEDUCT", 1, "initiatingDeduct");
    }
    
    @Override
    public R querySignStatus(String externalAgreementNo) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(fyConfig)) {
            return R.fail("301003","签约代扣功能未配置相关信息！请联系客服处理");
        }
        
        R queried = queryInterfaceForInstallmentRecord(externalAgreementNo, fyConfig);
        if (!queried.isSuccess()) {
            return queried;
        }
        
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(externalAgreementNo);
        FyQuerySignAgreementRsp rsp = (FyQuerySignAgreementRsp) queried.getData();
        
        if (Objects.equals(Integer.valueOf(rsp.getStatus()), SIGN_QUERY_STATUS_SIGN)) {
            handleSign(installmentRecord, rsp.getAgreementNo());
        } else if (Objects.equals(Integer.valueOf(rsp.getStatus()), SIGN_QUERY_STATUS_CANCEL)) {
            handleTerminating(installmentRecord);
        }
        
        return R.ok();
    }
    
    @Override
    public R terminateRecord(String externalAgreementNo) {
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(externalAgreementNo);
        if (Objects.isNull(installmentRecord)) {
            return R.fail("签约记录为空");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(installmentRecord.getUid());
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return R.fail("未退还电池");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        InstallmentTerminatingRecord installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, null);
        installmentTerminatingRecordService.insert(installmentTerminatingRecord);
        terminatingInstallmentRecord(installmentRecord);
        
        return R.ok();
    }
    
    @Override
    public R<String> handleTerminatingRecord(HandleTerminatingRecordQuery query) {
        InstallmentTerminatingRecord terminatingRecord = installmentTerminatingRecordService.queryById(query.getId());
        if (Objects.isNull(terminatingRecord)) {
            return R.fail("解约记录为空");
        }
        
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(terminatingRecord.getExternalAgreementNo());
        if (Objects.isNull(installmentRecord)) {
            return R.fail("签约记录为空");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(installmentRecord.getUid());
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return R.fail("未退还电池");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        InstallmentTerminatingRecord terminatingRecordUpdate = new InstallmentTerminatingRecord();
        terminatingRecordUpdate.setId(query.getId());
        terminatingRecordUpdate.setOpinion(query.getOpinion());
        terminatingRecordUpdate.setAuditorId(SecurityUtils.getUid());
        terminatingRecordUpdate.setUpdateTime(System.currentTimeMillis());
        
        if (Objects.equals(query.getStatus(), TERMINATING_RECORD_STATUS_REFUSE)) {
            terminatingRecordUpdate.setStatus(TERMINATING_RECORD_STATUS_REFUSE);
            installmentTerminatingRecordService.update(terminatingRecordUpdate);
            
            return R.ok();
        } else {
            terminatingRecordUpdate.setStatus(TERMINATING_RECORD_STATUS_RELEASE);
            installmentTerminatingRecordService.update(terminatingRecordUpdate);
            
            return terminatingInstallmentRecord(installmentRecord);
        }
    }
    
    @Override
    public R queryDeductStatus(String payNo) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(fyConfig)) {
            return R.fail("签约代扣功能未配置相关信息！请联系客服处理");
        }
        
        InstallmentDeductionRecord installmentDeductionRecord = installmentDeductionRecordService.queryByPayNo(payNo);
        
        // 调用蜂云接口查询结果
        R queried = queryInterfaceForDeductionRecord(payNo, fyConfig, installmentDeductionRecord.getExternalAgreementNo());
        if (!queried.isSuccess()) {
            return queried;
        }
        
        FyQueryAgreementPayRsp rsp = (FyQueryAgreementPayRsp) queried.getData();
        if (Objects.equals(rsp.getStatus(), AGREEMENT_PAY_QUERY_STATUS_SUCCESS)) {
            // 处理成功的场景
            handleAgreementPaySuccess(installmentDeductionRecord, rsp.getTradeNo());
        }
        return R.ok();
    }
    
    @Override
    public R<String> deduct(Long id) {
        try {
            Long uid = SecurityUtils.getUid();
            
            InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryById(id);
            if (Objects.isNull(deductionPlan)) {
                return R.fail("301011", "代扣计划不存在");
            }
            
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(deductionPlan.getExternalAgreementNo());
            
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.warn("INSTALLMENT DEDUCT WARN! The user is not associated with a tenant. uid={}", uid);
                return R.fail("301004", "请购买分期套餐后，再签约");
            }
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(tenant.getId());
            if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
                return R.fail("301003", "签约代扣功能未配置相关信息！请联系客服处理");
            }
            
            // 发起代扣
            Triple<Boolean, String, Object> initiatingDeductTriple = initiatingDeduct(deductionPlan, installmentRecord, fyConfig);
            
            if (Objects.nonNull(initiatingDeductTriple)) {
                return initiatingDeductTriple.getLeft() ? R.ok() : R.fail(initiatingDeductTriple.getMiddle());
            }
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR!", e);
        }
        return R.fail("301006", "代扣失败");
    }
    
    @Override
    public R<String> sign(InstallmentSignQuery query, HttpServletRequest request) {
        Long uid = null;
        try {
            uid = SecurityUtils.getUid();
            InstallmentRecord installmentRecord = installmentRecordService.queryRecordWithStatusForUser(uid,
                    Arrays.asList(INSTALLMENT_RECORD_STATUS_INIT, INSTALLMENT_RECORD_STATUS_UN_SIGN));
            if (Objects.isNull(installmentRecord)) {
                return R.fail("301004", "请购买分期套餐后，再签约");
            }
            
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.warn("INSTALLMENT SIGN WARN! The user is not associated with a tenant. uid={}", uid);
                return R.fail("301004", "请购买分期套餐后，再签约");
            }
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(tenant.getId());
            if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
                return R.fail("301003", "签约功能未配置相关信息！请联系客服处理");
            }
            
            if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_UN_SIGN)) {
                return R.ok(redisService.get(String.format(CACHE_INSTALLMENT_FORM_BODY, uid)));
            }
            
            if (Objects.isNull(query.getMobile()) || Objects.isNull(query.getUserName())) {
                return R.fail("301009", "初次签约需要输入用户信息");
            }
            
            FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(installmentRecord.getTenantId());
            if (Objects.isNull(freeDepositData) || Objects.isNull(freeDepositData.getByStagesCapacity()) || freeDepositData.getByStagesCapacity() < 1) {
                return R.fail("301010", "分期签约次数不足，请充值");
            }
            
            Vars vars = new Vars();
            vars.setUserName(query.getUserName());
            vars.setMobile(query.getMobile());
            vars.setProvinceName("陕西省");
            vars.setCityName("西安市");
            vars.setDistrictName("未央区");
            
            FySignAgreementRequest agreementRequest = new FySignAgreementRequest();
            agreementRequest.setChannelFrom(CHANNEL_FROM_H5);
            agreementRequest.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
            agreementRequest.setMerchantName(tenant.getName());
            agreementRequest.setServiceName("分期签约");
            agreementRequest.setServiceDescription("分期签约");
            agreementRequest.setNotifyUrl(String.format(fengYunConfig.getInstallmentNotifyUrl(), uid));
            agreementRequest.setVars(JsonUtil.toJson(vars));
            
            FyCommonQuery<FySignAgreementRequest> commonQuery = new FyCommonQuery<>();
            commonQuery.setChannelCode(fyConfig.getChannelCode());
            commonQuery.setFlowNo(installmentRecord.getExternalAgreementNo() + System.currentTimeMillis());
            commonQuery.setFyRequest(agreementRequest);
            FyResult<FySignAgreementRsp> fySignResult = fyAgreementService.signAgreement(commonQuery);
            
            if (FY_SUCCESS_CODE.equals(fySignResult.getCode())) {
                
                // 更新签约记录状态位为待签约，需要与初始化的订单区分开
                InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
                installmentRecordUpdate.setId(installmentRecord.getId());
                installmentRecordUpdate.setUserName(query.getUserName());
                installmentRecordUpdate.setMobile(query.getMobile());
                installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_UN_SIGN);
                installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
                installmentRecordService.update(installmentRecordUpdate);
                
                // 二维码缓存3天，利用zSet实现延时取消签约，分数为三天后的当前时刻减去2分钟
                // TODO SJP 自动取消签约时间目前设置5分钟，上线时设置三天后的当前时刻减去2分钟
                double score = (double) Instant.now().plus(5, ChronoUnit.MINUTES).minus(2, ChronoUnit.MINUTES).toEpochMilli();
                redisService.zsetAddString(CACHE_INSTALLMENT_CANCEL_SIGN, installmentRecord.getExternalAgreementNo(), score);
                redisService.saveWithString(String.format(CACHE_INSTALLMENT_FORM_BODY, uid), fySignResult.getFyResponse().getFormBody(), 3L,
                        TimeUnit.DAYS);
                log.info("取消签约定时任务调试，存入请求签约号2，externalAgreementNo={}，score={}", installmentRecord.getExternalAgreementNo(), score);
                
                // 扣减次数
                FreeDepositData freeDepositDataUpdate = new FreeDepositData();
                freeDepositDataUpdate.setId(freeDepositData.getId());
                Integer remaining = freeDepositData.getByStagesCapacity() - 1;
                freeDepositDataUpdate.setByStagesCapacity(remaining);
                freeDepositDataService.update(freeDepositDataUpdate);
                // 发送站内信
                siteMessagePublish.publish(SiteMessageEvent.builder(this).code(SiteMessageType.INSUFFICIENT_RECHARGE_BALANCE).notifyTime(System.currentTimeMillis())
                        .tenantId(installmentRecord.getTenantId().longValue()).addContext("type", RechargeAlarm.AUTH_PAY.getCode()).addContext("count", remaining).build());
                
                return R.ok(fySignResult.getFyResponse().getFormBody());
            }
        } catch (Exception e) {
            log.error("INSTALLMENT SIGN ERROR! uid={}", uid, e);
        }
        return R.fail("301002", "签约失败，请联系管理员");
    }
    
    @Override
    public R<String> handleSign(InstallmentRecord installmentRecord, String agreementNo) {
        if (Objects.isNull(installmentRecord) || !INSTALLMENT_RECORD_STATUS_UN_SIGN.equals(installmentRecord.getStatus())) {
            log.warn("SIGN NOTIFY WARN! no right installmentRecord, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
            return R.fail(null);
        }
        
        if (!redisService.setNx(String.format(CACHE_INSTALLMENT_SIGN_NOTIFY_LOCK, installmentRecord.getUid()), "1", 3 * 1000L, false)) {
            log.info("回调调试，获取锁失败");
            return R.ok();
        }
        
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_SIGN)) {
            log.info("回调调试，已签约");
            return R.ok();
        }
        log.info("回调调试，修改签约记录状态");
        // 更新签约记录状态
        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_SIGN);
        installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentRecordUpdate.setAgreementNo(agreementNo);
        
        log.info("回调调试，生成还款计划");
        // 生成还款计划
        List<InstallmentDeductionPlan> deductionPlanList = installmentDeductionPlanService.generateDeductionPlan(installmentRecord);
        if (Objects.isNull(deductionPlanList)) {
            log.warn("SIGN NOTIFY WARN! generate deduction plan, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
        }
        
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
        if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
            log.warn("SIGN NOTIFY WARN! fyConfig wrong, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
        }
        
        // 尽快给用户完成代扣和套餐绑定，异步发起代扣
        installmentRecord.setAgreementNo(agreementNo);
        initiatingDeductThreadPool.execute(() -> {
            log.info("回调调试，异步代扣");
            initiatingDeduct(deductionPlanList.get(0), installmentRecord, fyConfig);
        });
        
        // 更新签约记录
        installmentRecordService.update(installmentRecordUpdate);
        // 保存还款计划
        deductionPlanList.forEach(installmentDeductionPlanService::insert);
        
        log.info("回调调试，删除缓存");
        // 签约成功，删除签约二维码缓存
        redisService.delete(String.format(CACHE_INSTALLMENT_FORM_BODY, installmentRecord.getUid()));
        
        return R.ok();
    }
    
    @Override
    public R<String> handleTerminating(InstallmentRecord installmentRecord) {
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_CANCELLED)) {
            return R.ok();
        }
        
        // 更新签约记录
        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_CANCELLED);
        installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
        
        InstallmentDeductionPlanQuery deductionPlanQuery = new InstallmentDeductionPlanQuery();
        deductionPlanQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        deductionPlanQuery.setStatus(DEDUCTION_PLAN_STATUS_INIT);
        
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(deductionPlanQuery).getData();
        
        installmentRecordService.update(installmentRecordUpdate);
        if (!CollectionUtils.isEmpty(deductionPlans)) {
            deductionPlans.parallelStream().forEach(deductionPlan -> {
                InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
                deductionPlanUpdate.setId(deductionPlan.getId());
                deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_CANCEL);
                deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
                installmentDeductionPlanService.update(deductionPlanUpdate);
            });
        }
        return R.ok();
    }
    
    @Override
    public R handleAgreementPaySuccess(InstallmentDeductionRecord deductionRecord, String tradeNo) {
        if (Objects.equals(deductionRecord.getStatus(), DEDUCTION_RECORD_STATUS_SUCCESS)) {
            return R.ok();
        }
        
        if (!redisService.setNx(String.format(CACHE_INSTALLMENT_AGREEMENT_PAY_NOTIFY_LOCK, deductionRecord.getUid()), "1", 3 * 1000L, false)) {
            return R.ok();
        }
        
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(deductionRecord.getExternalAgreementNo());
        
        InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryByAgreementNoAndIssue(deductionRecord.getExternalAgreementNo(), deductionRecord.getIssue());
        
        Triple<Boolean, String, Object> handlePackageTriple = null;
        if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
            // 处理换电代扣成功的场景
            handlePackageTriple = handleBatteryMemberCard(deductionRecord, installmentRecord, deductionPlan, deductionRecord.getUid());
        }
        
        // 代扣成功后其他记录的处理
        if (Objects.nonNull(handlePackageTriple) && handlePackageTriple.getLeft()) {
            InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
            deductionPlanUpdate.setId(deductionPlan.getId());
            deductionPlanUpdate.setTradeNo(tradeNo);
            deductionPlanUpdate.setPayNo(deductionRecord.getPayNo());
            deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_PAID);
            deductionPlanUpdate.setPaymentTime(System.currentTimeMillis());
            deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
            installmentDeductionPlanService.update(deductionPlanUpdate);
            
            InstallmentDeductionRecord deductionRecordUpdate = new InstallmentDeductionRecord();
            deductionRecordUpdate.setId(deductionRecord.getId());
            deductionRecordUpdate.setStatus(DEDUCTION_RECORD_STATUS_SUCCESS);
            deductionRecordUpdate.setUpdateTime(System.currentTimeMillis());
            installmentDeductionRecordService.update(deductionRecordUpdate);
            
            InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
            installmentRecordUpdate.setId(installmentRecord.getId());
            // 若全部代扣完，改为已完成，并且解约
            if (Objects.equals(installmentRecord.getInstallmentNo(), deductionRecord.getIssue())) {
                installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_COMPLETED);
            }
            installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
            installmentRecordUpdate.setPaidInstallment(installmentRecord.getPaidInstallment() + 1);
            installmentRecordUpdate.setPaidAmount(installmentRecord.getPaidAmount().add(deductionRecord.getAmount()));
            installmentRecordService.update(installmentRecordUpdate);
            
            if (Objects.equals(installmentRecord.getInstallmentNo(), deductionRecord.getIssue())) {
                InstallmentTerminatingRecord installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, "分期套餐代扣完毕");
                installmentTerminatingRecordService.insert(installmentTerminatingRecord);
                terminatingInstallmentRecord(installmentRecord);
            }
        }
        return R.ok();
    }
    
    @Override
    public Triple<Boolean, String, Object> initiatingDeduct(InstallmentDeductionPlan deductionPlan, InstallmentRecord installmentRecord, FyConfig fyConfig) {
        log.info("回调调试，代扣开始，deductionPlan={}", deductionPlan);
        if (!redisService.setNx(String.format(CACHE_INSTALLMENT_DEDUCT_LOCK, installmentRecord.getUid()), "1", 3 * 1000L, false)) {
            log.info("回调调试，代扣获取锁失败");
            return Triple.of(false, "已对该用户执行代扣，请稍候再试", null);
        }
        
        // payNo仅有20个字符，用uid加时间秒值不会重复
        String payNo = String.format("%08d", installmentRecord.getUid()) + (System.currentTimeMillis() / 1000);
        String repaymentPlanNo = OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SIGN_AGREEMENT_PAY, installmentRecord.getUid());
        
        // 生成代扣记录
        InstallmentDeductionRecord installmentDeductionRecord = new InstallmentDeductionRecord();
        installmentDeductionRecord.setUid(installmentRecord.getUid());
        installmentDeductionRecord.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        installmentDeductionRecord.setPayNo(payNo);
        installmentDeductionRecord.setRepaymentPlanNo(repaymentPlanNo);
        installmentDeductionRecord.setUserName(installmentRecord.getUserName());
        installmentDeductionRecord.setMobile(installmentRecord.getMobile());
        installmentDeductionRecord.setAmount(deductionPlan.getAmount());
        installmentDeductionRecord.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        installmentDeductionRecord.setIssue(deductionPlan.getIssue());
        installmentDeductionRecord.setSubject(null);
        installmentDeductionRecord.setTenantId(deductionPlan.getTenantId());
        installmentDeductionRecord.setFranchiseeId(deductionPlan.getFranchiseeId());
        installmentDeductionRecord.setCreateTime(System.currentTimeMillis());
        installmentDeductionRecord.setUpdateTime(System.currentTimeMillis());
        installmentDeductionRecordService.insert(installmentDeductionRecord);
        
        try {
            
            FyCommonQuery<FyAgreementPayRequest> fyCommonQuery = new FyCommonQuery<>();
            
            FyAgreementPayRequest request = new FyAgreementPayRequest();
            
            request.setPayNo(payNo);
            request.setAgreementNo(installmentRecord.getAgreementNo());
            request.setRepaymentPlanNo(repaymentPlanNo);
            request.setTotalAmount(deductionPlan.getAmount().multiply(new BigDecimal("100")).intValue());
            request.setSubject("分期套餐代扣支付");
            request.setNotifyUrl(String.format(fengYunConfig.getAgreementPayNotifyUrl(), installmentRecord.getUid()));
            request.setUserName(installmentRecord.getUserName());
            request.setMobile(installmentRecord.getMobile());
            request.setProvinceName("陕西省");
            request.setCityName("西安市");
            
            log.info("回调调试，代扣参数，request={}", request);
            
            fyCommonQuery.setChannelCode(fyConfig.getChannelCode());
            fyCommonQuery.setFlowNo(repaymentPlanNo + System.currentTimeMillis());
            fyCommonQuery.setFyRequest(request);
            FyResult<FyAgreementPayRsp> fyAgreementPayRspFyResult = fyAgreementService.agreementPay(fyCommonQuery);
            
            log.info("回调调试，代扣结果，fyAgreementPayRspFyResult={}", fyAgreementPayRspFyResult);
            
            // 调用成功
            if (Objects.equals(FY_SUCCESS_CODE, fyAgreementPayRspFyResult.getCode())) {
                return Triple.of(true, null, null);
            }
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR!", e);
        }
        
        InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
        deductionPlanUpdate.setId(deductionPlan.getId());
        deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
        deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_FAIL);
        installmentDeductionPlanService.update(deductionPlanUpdate);
        
        // 报错或调用失败则保存代扣失败的记录
        InstallmentDeductionRecord deductionRecordUpdate = new InstallmentDeductionRecord();
        deductionRecordUpdate.setId(installmentDeductionRecord.getId());
        deductionRecordUpdate.setStatus(DEDUCTION_RECORD_STATUS_FAIL);
        deductionRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentDeductionRecordService.update(deductionRecordUpdate);
        return Triple.of(false, "代扣失败", null);
    }
    
    @Override
    public R<String> createTerminatingRecord(String externalAgreementNo, String reason) {
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(externalAgreementNo);
        if (Objects.isNull(installmentRecord) || Arrays.asList(INSTALLMENT_RECORD_STATUS_COMPLETED, INSTALLMENT_RECORD_STATUS_CANCELLED).contains(installmentRecord.getStatus())) {
            log.info("CREATE TERMINATING RECORD INFO! Record cancellation externalAgreementNo={}", externalAgreementNo);
            return R.fail("301012", "分期套餐已解约");
        }
        
        Integer num = (Integer) eleRefundOrderService.queryCount(
                EleRefundQuery.builder().uid(installmentRecord.getUid()).statuses(Arrays.asList(EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUND)).build()).getData();
        if (Objects.nonNull(num) && num > 0) {
            return R.fail("301013", "有未完成的押金退款订单");
        }
        
        InstallmentTerminatingRecordQuery query = new InstallmentTerminatingRecordQuery();
        query.setUid(installmentRecord.getUid());
        query.setStatuses(List.of(TERMINATING_RECORD_STATUS_INIT));
        
        List<InstallmentTerminatingRecord> records = installmentTerminatingRecordService.listForRecordWithStatus(query);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(records)) {
            return R.fail("301014", "有未完成的解约申请");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        InstallmentTerminatingRecord installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, reason);
        installmentTerminatingRecordService.insert(installmentTerminatingRecord);
        
        return R.ok();
    }
    
    
    private Triple<Boolean, String, Object> handleBatteryMemberCard(InstallmentDeductionRecord deductionRecord, InstallmentRecord installmentRecord,
            InstallmentDeductionPlan deductionPlan, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        
        ElectricityMemberCardOrder memberCardOrder = electricityMemberCardOrderService.queryOrderByAgreementNoAndIssue(deductionRecord.getExternalAgreementNo(), 1);
        
        // 给用户绑定套餐
        if (Objects.equals(deductionRecord.getIssue(), 1)) {
            ElectricityMemberCardOrder memberCardOrderUpdate = new ElectricityMemberCardOrder();
            memberCardOrderUpdate.setId(memberCardOrder.getId());
            memberCardOrderUpdate.setValidDays(deductionPlan.getRentTime());
            electricityMemberCardOrderService.updateByID(memberCardOrderUpdate);
            
            unionTradeOrderService.manageMemberCardOrderV2(memberCardOrder.getOrderId(), ElectricityTradeOrder.STATUS_SUCCESS);
        } else {
            // 下述校验在上文的绑定第一期套餐方法内部做了，故在此处校验
            if (Objects.isNull(userInfo)) {
                log.warn("NOTIFY AGREEMENT PAY WARN!userInfo is null,uid={}", uid);
                return Triple.of(false, "用户不存在", null);
            }
            
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("NOTIFY AGREEMENT PAY WARN!batteryMemberCard is null,uid={},mid={}", uid, installmentRecord.getPackageId());
                return Triple.of(false, "套餐不存在", null);
            }
            electricityMemberCardOrderService.saveRenewalUserBatteryMemberCardOrder(null, userInfo, batteryMemberCard, userBatteryMemberCard, batteryMemberCard, installmentRecord,
                    memberCardOrder.getSource());
        }
        
        return Triple.of(true, null, null);
    }
    
    
    private R queryInterfaceForInstallmentRecord(String externalAgreementNo, FyConfig fyConfig) {
        try {
            FyCommonQuery<FyQuerySignAgreementRequest> commonQuery = new FyCommonQuery<>();
            
            FyQuerySignAgreementRequest request = new FyQuerySignAgreementRequest();
            request.setExternalAgreementNo(externalAgreementNo);
            
            commonQuery.setChannelCode(fyConfig.getChannelCode());
            commonQuery.setFlowNo(externalAgreementNo + System.currentTimeMillis());
            commonQuery.setFyRequest(request);
            FyResult<FyQuerySignAgreementRsp> result = fyAgreementService.querySignAgreement(commonQuery);
            if (Objects.equals(result.getCode(), FY_SUCCESS_CODE)) {
                return R.ok(result.getFyResponse());
            }
        } catch (Exception e) {
            log.error("QUERY INSTALLMENT RECORD STATUS ERROR!", e);
        }
        return R.fail("查询失败");
    }
    
    private R<String> terminatingInstallmentRecord(InstallmentRecord installmentRecord) {
        try {
            FyConfig config = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
            if (Objects.isNull(config)) {
                return R.fail("租户分期配置不存在");
            }
            
            FyCommonQuery<FyReleaseAgreementRequest> commonQuery = new FyCommonQuery<>();
            FyReleaseAgreementRequest request = new FyReleaseAgreementRequest();
            request.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
            request.setNotifyUrl(String.format(fengYunConfig.getInstallmentNotifyUrl(), installmentRecord.getUid()));
            
            commonQuery.setChannelCode(config.getChannelCode());
            commonQuery.setFlowNo(installmentRecord.getExternalAgreementNo() + System.currentTimeMillis());
            commonQuery.setFyRequest(request);
            
            FyResult<FyReleaseAgreementRsp> result = fyAgreementService.releaseAgreement(commonQuery);
            if (!Objects.equals(result.getCode(), FY_SUCCESS_CODE)) {
                return R.fail("解约失败");
            }
            
            InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
            installmentRecordUpdate.setId(installmentRecord.getId());
            installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_TERMINATE);
            installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
            installmentRecordService.update(installmentRecordUpdate);
            
            return R.ok();
        } catch (Exception e) {
            log.error("TERMINATING INSTALLMENT RECORD ERROR!", e);
        }
        return R.fail("解约失败");
    }
    
    private R queryInterfaceForDeductionRecord(String payNo, FyConfig fyConfig, String externalAgreementNo) {
        try {
            FyCommonQuery<FyQueryAgreementPayRequest> commonQuery = new FyCommonQuery<>();
            
            FyQueryAgreementPayRequest request = new FyQueryAgreementPayRequest();
            request.setPayNo(payNo);
            
            commonQuery.setChannelCode(fyConfig.getChannelCode());
            commonQuery.setFlowNo(externalAgreementNo + System.currentTimeMillis());
            commonQuery.setFyRequest(request);
            FyResult<FyQueryAgreementPayRsp> result = fyAgreementService.queryAgreementPay(commonQuery);
            
            if (Objects.equals(result.getCode(), FY_SUCCESS_CODE)) {
                // 传递结果给外部方法校验
                return R.ok(result.getFyResponse());
            }
        } catch (Exception e) {
            log.error("QUERY INSTALLMENT RECORD STATUS ERROR!", e);
        }
        return R.fail("查询失败");
    }
}
