package com.xiliulou.electricity.service.impl.installment;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.InstallmentMqCommonDTO;
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
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
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
import com.xiliulou.electricity.service.installment.InstallmentSearchApiService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQueryAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQuerySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyReleaseAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.Vars;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQueryAgreementPayRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQuerySignAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyReleaseAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyResult;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FySignAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.service.FyAgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_CANCEL_SIGN;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_DEDUCT_LOCK;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_FORM_BODY;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_SIGN_CANCEL_LOCK;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_SIGN_NOTIFY_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.AGREEMENT_PAY_QUERY_STATUS_SUCCESS;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_CANCEL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_PAID;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_SUCCESS;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.FY_RESULT_CODE_SUCCESS;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_CANCELLED;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_CANCEL_PAY;
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
    
    private final RocketMqService rocketMqService;
    
    private final InstallmentSearchApiService installmentSearchApiService;
    
    private XllThreadPoolExecutorService initiatingDeductThreadPool;
    
    private InheritableThreadLocal<String> inheritableThreadLocal;
    
    @PostConstruct
    public void init() {
        initiatingDeductThreadPool = XllThreadPoolExecutors.newFixedThreadPool("INSTALLMENT_INITIATING_DEDUCT", 1, "initiatingDeduct");
        inheritableThreadLocal = new InheritableThreadLocal<>();
    }
    
    @Override
    public R<String> querySignStatus(String externalAgreementNo) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(fyConfig)) {
            return R.fail("301003", "签约代扣功能未配置相关信息！请联系客服处理");
        }
        
        R<Object> queried = queryInterfaceForInstallmentRecord(externalAgreementNo, fyConfig);
        if (!queried.isSuccess()) {
            return R.fail("301021", "查询失败");
        }
        
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
        FyQuerySignAgreementRsp rsp = (FyQuerySignAgreementRsp) queried.getData();
        
        if (Objects.equals(Integer.valueOf(rsp.getStatus()), SIGN_QUERY_STATUS_SIGN)) {
            handleSign(installmentRecord, rsp.getAgreementNo());
        } else if (Objects.equals(Integer.valueOf(rsp.getStatus()), SIGN_QUERY_STATUS_CANCEL)) {
            handleTerminating(installmentRecord);
        }
        
        return R.ok();
    }
    
    @Override
    public R<String> terminateRecord(String externalAgreementNo) {
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
        if (Objects.isNull(installmentRecord)) {
            return R.fail("301005", "签约记录不存在");
        }
        
        Integer num = (Integer) eleRefundOrderService.queryCount(
                EleRefundQuery.builder().uid(installmentRecord.getUid()).statuses(Arrays.asList(EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUND)).build()).getData();
        if (Objects.nonNull(num) && num > 0) {
            return R.fail("301013", "有未完成的押金退款");
        }
        
        FyConfig config = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
        if (Objects.isNull(config)) {
            return R.fail("301024", "解约功能未配置相关信息！请联系客服处理");
        }
        
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_INIT) || Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_UN_SIGN)) {
            return R.ok();
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(installmentRecord.getUid());
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return R.fail("301017", "未退还电池");
        }
        
        InstallmentTerminatingRecordQuery query = new InstallmentTerminatingRecordQuery();
        query.setUid(installmentRecord.getUid());
        query.setStatuses(List.of(TERMINATING_RECORD_STATUS_INIT));
        
        List<InstallmentTerminatingRecord> records = installmentTerminatingRecordService.listForUserWithStatus(query);
        if (!CollectionUtils.isEmpty(records)) {
            return R.fail("301014", "有未完成的解约申请");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        recordQuery.setUid(installmentRecord.getUid());
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        InstallmentTerminatingRecord installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, "后台解约", false);
        installmentTerminatingRecord.setAuditorId(SecurityUtils.getUid());
        installmentTerminatingRecordService.insert(installmentTerminatingRecord);
        
        R<String> terminatingR = terminatingInstallmentRecord(installmentRecord, config);
        if (terminatingR.isSuccess()) {
            return terminatingR;
        }
        
        InstallmentTerminatingRecord terminatingRecordUpdate = new InstallmentTerminatingRecord();
        terminatingRecordUpdate.setId(installmentTerminatingRecord.getId());
        terminatingRecordUpdate.setStatus(TERMINATING_RECORD_STATUS_REFUSE);
        terminatingRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentTerminatingRecordService.update(terminatingRecordUpdate);
        return terminatingR;
    }
    
    @Override
    public R<String> handleTerminatingRecord(HandleTerminatingRecordQuery query) {
        InstallmentTerminatingRecord terminatingRecord = installmentTerminatingRecordService.queryById(query.getId());
        if (Objects.isNull(terminatingRecord)) {
            return R.fail("301018", "解约申请不存在");
        }
        
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(terminatingRecord.getExternalAgreementNo());
        if (Objects.isNull(installmentRecord)) {
            return R.fail("301005", "签约记录不存在");
        }
        
        FyConfig config = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
        if (Objects.isNull(config)) {
            return R.fail("301024", "解约功能未配置相关信息！请联系客服处理");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(installmentRecord.getUid());
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return R.fail("301017", "未退还电池");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        recordQuery.setUid(installmentRecord.getUid());
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
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
            R<String> terminatingR = terminatingInstallmentRecord(installmentRecord, config);
            if (!terminatingR.isSuccess()) {
                return terminatingR;
            }
            terminatingRecordUpdate.setStatus(TERMINATING_RECORD_STATUS_RELEASE);
            installmentTerminatingRecordService.update(terminatingRecordUpdate);
            
            return terminatingR;
        }
    }
    
    @Override
    public R<Object> queryDeductStatus(String payNo) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(fyConfig)) {
            return R.fail("301003", "签约代扣功能未配置相关信息！请联系客服处理");
        }
        
        InstallmentDeductionRecord installmentDeductionRecord = installmentDeductionRecordService.queryByPayNo(payNo);
        
        // 调用蜂云接口查询结果
        R<Object> queried = queryInterfaceForDeductionRecord(payNo, fyConfig, installmentDeductionRecord.getExternalAgreementNo());
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
    public R<String> deduct(List<InstallmentDeductionPlan> deductionPlans) {
        try {
            Long uid = SecurityUtils.getUid();
            
            if (CollectionUtils.isEmpty(deductionPlans)) {
                log.warn("INSTALLMENT DEDUCT WARN! deductionPlans is null. uid={}", uid);
                return R.fail("301011", "代扣计划不存在");
            }
            
            String externalAgreementNo = deductionPlans.get(0).getExternalAgreementNo();
            
            InstallmentDeductionRecordQuery query = new InstallmentDeductionRecordQuery();
            query.setExternalAgreementNo(externalAgreementNo);
            query.setStatus(DEDUCTION_RECORD_STATUS_INIT);
            query.setUid(uid);
            List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(query);
            if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
                log.warn("INSTALLMENT DEDUCT WARN! deductionPlans is running. uid={}", uid);
                return R.fail("301020", "当前有正在执行中的分期代扣，请稍后再试");
            }
            
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
            if (Objects.isNull(installmentRecord)) {
                log.warn("INSTALLMENT DEDUCT WARN! installmentRecord is null. uid={}", uid);
                return R.fail("301050", "签约记录状态异常");
            }
            
            if (Objects.equals(installmentRecord.getInstallmentNo(), installmentRecord.getPaidInstallment())) {
                log.warn("INSTALLMENT DEDUCT WARN! The installmentRecord has been withheld. uid={}", uid);
                return R.fail("301026", "分期套餐已代扣完毕");
            }
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
                log.warn("INSTALLMENT DEDUCT WARN! fyConfig is wrong. uid={}", uid);
                return R.fail("301003", "签约代扣功能未配置相关信息！请联系客服处理");
            }
            
            // 发起代扣
            Triple<Boolean, String, Object> initiatingDeductTriple = initiatingDeduct(deductionPlans, installmentRecord, fyConfig);
            
            if (Objects.nonNull(initiatingDeductTriple)) {
                return initiatingDeductTriple.getLeft() ? R.ok() : R.fail(initiatingDeductTriple.getMiddle(), (String) initiatingDeductTriple.getRight());
            }
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR!", e);
        }
        return R.fail("301006", "代扣失败");
    }
    
    @Override
    public R<String> sign(InstallmentSignQuery query, HttpServletRequest request, String channelFrom) {
        Long uid = null;
        try {
            uid = SecurityUtils.getUid();
            if (!redisService.setNx(String.format(CACHE_INSTALLMENT_SIGN_CANCEL_LOCK, uid), "1", 3 * 1000L, false)) {
                return R.fail("301019", "当前套餐正在签约或取消，请稍候再试");
            }
            
            InstallmentRecord installmentRecord = installmentSearchApiService.queryRecordWithStatusForUser(uid,
                    Arrays.asList(INSTALLMENT_RECORD_STATUS_INIT, INSTALLMENT_RECORD_STATUS_UN_SIGN));
            if (Objects.isNull(installmentRecord)) {
                return R.fail("301004", "请购买分期套餐成功后，再签约");
            }
            
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.warn("INSTALLMENT SIGN WARN! The user is not associated with a tenant. uid={}", uid);
                return R.fail("301004", "请购买分期套餐成功后，再签约");
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
            
            String packageName = "";
            if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
                packageName = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId()).getName();
            }
            
            Vars vars = new Vars();
            vars.setUserName(query.getUserName());
            vars.setMobile(query.getMobile());
            vars.setProvinceName("陕西省");
            vars.setCityName("西安市");
            vars.setDistrictName("未央区");
            
            String description = "订单编号:%s；套餐名称:%s";
            
            FySignAgreementRequest agreementRequest = new FySignAgreementRequest();
            agreementRequest.setChannelFrom(channelFrom);
            agreementRequest.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
            agreementRequest.setMerchantName(tenant.getName());
            agreementRequest.setServiceName(packageName);
            agreementRequest.setServiceDescription(String.format(description, installmentRecord.getExternalAgreementNo(), packageName));
            agreementRequest.setNotifyUrl(String.format(fengYunConfig.getInstallmentNotifyUrl(), uid));
            agreementRequest.setVars(JsonUtil.toJson(vars));
            
            FyCommonQuery<FySignAgreementRequest> commonQuery = new FyCommonQuery<>();
            commonQuery.setChannelCode(fyConfig.getChannelCode());
            commonQuery.setFlowNo(installmentRecord.getExternalAgreementNo() + System.currentTimeMillis());
            commonQuery.setFyRequest(agreementRequest);
            FyResult<FySignAgreementRsp> fySignResult = fyAgreementService.signAgreement(commonQuery);
            
            if (FY_RESULT_CODE_SUCCESS.equals(fySignResult.getCode())) {
                
                // 更新签约记录状态位为待签约，需要与初始化的订单区分开
                InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
                installmentRecordUpdate.setId(installmentRecord.getId());
                installmentRecordUpdate.setUserName(query.getUserName());
                installmentRecordUpdate.setMobile(query.getMobile());
                installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_UN_SIGN);
                installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
                installmentRecordService.update(installmentRecordUpdate);
                
                // 二维码缓存3天，利用zSet实现延时取消签约，分数为三天后的当前时刻减去10分钟
                double score = (double) Instant.now().plus(3, ChronoUnit.DAYS).minus(10, ChronoUnit.MINUTES).toEpochMilli();
                redisService.zsetAddString(CACHE_INSTALLMENT_CANCEL_SIGN, installmentRecord.getExternalAgreementNo(), score);
                redisService.saveWithString(String.format(CACHE_INSTALLMENT_FORM_BODY, uid), fySignResult.getFyResponse().getFormBody(), 3L, TimeUnit.DAYS);
                
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
            } else {
                log.info("INSTALLMENT SIGN FAIL! uid={}, fySignResult={}", uid, fySignResult);
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
            return R.ok();
        }
        
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_SIGN)) {
            return R.ok();
        }
        // 更新签约记录状态
        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_SIGN);
        installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentRecordUpdate.setAgreementNo(agreementNo);
        
        // 生成还款计划
        List<InstallmentDeductionPlan> deductionPlanList = installmentDeductionPlanService.generateDeductionPlan(installmentRecord);
        if (Objects.isNull(deductionPlanList)) {
            log.warn("SIGN NOTIFY WARN! generate deduction plan, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
        }
        
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
        if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
            log.warn("SIGN NOTIFY WARN! fyConfig wrong, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
        }
        
        // 更新签约记录
        installmentRecordService.update(installmentRecordUpdate);
        
        // 保存还款计划
        installmentDeductionPlanService.batchInsert(deductionPlanList);
        
        // 发送消息，异步执行代扣
        InstallmentMqCommonDTO commonDTO = new InstallmentMqCommonDTO();
        commonDTO.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        commonDTO.setTraceId(MDC.get(CommonConstant.TRACE_ID));
        rocketMqService.sendAsyncMsg(MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, JsonUtil.toJson(commonDTO), MqProducerConstant.INSTALLMENT_DEDUCT_TAG);
        
        // 签约成功，删除签约二维码缓存
        redisService.delete(String.format(CACHE_INSTALLMENT_FORM_BODY, installmentRecord.getUid()));
        
        return R.ok();
    }
    
    @Override
    public R<String> handleTerminating(InstallmentRecord installmentRecord) {
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_CANCELLED) || Objects.equals(installmentRecord.getStatus(),
                INSTALLMENT_RECORD_STATUS_COMPLETED)) {
            return R.ok();
        }
        
        // 更新代扣计划
        InstallmentDeductionPlanQuery deductionPlanQuery = new InstallmentDeductionPlanQuery();
        deductionPlanQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        deductionPlanQuery.setStatuses(Arrays.asList(DEDUCTION_PLAN_STATUS_INIT, DEDUCTION_PLAN_STATUS_FAIL));
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(deductionPlanQuery).getData();
        
        if (!CollectionUtils.isEmpty(deductionPlans)) {
            deductionPlans.parallelStream().forEach(deductionPlan -> {
                InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
                deductionPlanUpdate.setId(deductionPlan.getId());
                deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_CANCEL);
                deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
                installmentDeductionPlanService.update(deductionPlanUpdate);
            });
        }
        
        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        
        // 更新签约记录，如果是已完成的不更新成已解约
        if (Objects.equals(installmentRecord.getInstallmentNo(), installmentRecord.getPaidInstallment())) {
            installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_COMPLETED);
        } else {
            installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_CANCELLED);
        }
        installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentRecordService.update(installmentRecordUpdate);
        
        // 更新解约记录
        InstallmentTerminatingRecord terminatingRecord = installmentTerminatingRecordService.queryLatestByExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        InstallmentTerminatingRecord terminatingRecordUpdate = new InstallmentTerminatingRecord();
        terminatingRecordUpdate.setId(terminatingRecord.getId());
        terminatingRecordUpdate.setStatus(TERMINATING_RECORD_STATUS_RELEASE);
        terminatingRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentTerminatingRecordService.update(terminatingRecordUpdate);
        
        List<ElectricityMemberCardOrder> electricityMemberCardOrders = electricityMemberCardOrderService.listOrderByExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        if (CollectionUtils.isEmpty(electricityMemberCardOrders)) {
            return R.ok();
        }
        
        electricityMemberCardOrders.parallelStream().forEach(memberCardOrder -> {
            if (Objects.equals(memberCardOrder.getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS)) {
                return;
            }
            
            ElectricityMemberCardOrder memberCardOrderUpdate = new ElectricityMemberCardOrder();
            memberCardOrderUpdate.setId(memberCardOrder.getId());
            memberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_CANCEL);
            memberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityMemberCardOrderService.updateByID(memberCardOrderUpdate);
        });
        
        return R.ok();
    }
    
    @Override
    public void handleAgreementPaySuccess(InstallmentDeductionRecord deductionRecord, String tradeNo) {
        // 防止一个代扣计划多次处理
        if (Objects.equals(deductionRecord.getStatus(), DEDUCTION_RECORD_STATUS_SUCCESS)) {
            return;
        }
        
        String externalAgreementNo = deductionRecord.getExternalAgreementNo();
        String payNo = deductionRecord.getPayNo();
        
        InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryById(deductionRecord.getPlanId());
        if (Objects.isNull(deductionPlan)) {
            log.warn("HANDLE AGREEMENT PAY SUCCESS. deductionPlan is null, externalAgreementNo={}, payNo={}", externalAgreementNo, payNo);
            return;
        }
        
        // 每一个代扣计划的回调都将自己对应的代扣计划、代扣记录修改为代扣成功，并发送消息续套餐
        InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
        deductionPlanUpdate.setId(deductionPlan.getId());
        deductionPlanUpdate.setPayNo(deductionRecord.getPayNo());
        deductionPlanUpdate.setTradeNo(tradeNo);
        deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_PAID);
        deductionPlanUpdate.setPaymentTime(System.currentTimeMillis());
        deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
        installmentDeductionPlanService.update(deductionPlanUpdate);
        
        InstallmentDeductionRecord deductionRecordUpdate = new InstallmentDeductionRecord();
        deductionRecordUpdate.setId(deductionRecord.getId());
        deductionRecordUpdate.setStatus(DEDUCTION_RECORD_STATUS_SUCCESS);
        deductionRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentDeductionRecordService.update(deductionRecordUpdate);
        
        // 发送消息续费套餐
        InstallmentMqCommonDTO commonDTO = new InstallmentMqCommonDTO();
        commonDTO.setExternalAgreementNo(externalAgreementNo);
        commonDTO.setIssue(deductionPlan.getIssue());
        commonDTO.setAmount(deductionRecord.getAmount());
        commonDTO.setTraceId(MDC.get(CommonConstant.TRACE_ID));
        rocketMqService.sendAsyncMsg(MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, JsonUtil.toJson(commonDTO), MqProducerConstant.INSTALLMENT_DEDUCT_NOTIFY_TAG);
    }
    
    @Override
    public Triple<Boolean, String, Object> initiatingDeduct(List<InstallmentDeductionPlan> deductionPlans, InstallmentRecord installmentRecord, FyConfig fyConfig) {
        if (!redisService.setNx(String.format(CACHE_INSTALLMENT_DEDUCT_LOCK, installmentRecord.getUid()), "1", 30 * 1000L, false)) {
            return Triple.of(false, "301023", "操作频繁，请3秒后再试");
        }
        
        // 可能会有代扣了一半的重新发起，要把已经代扣了的过滤掉
        List<InstallmentDeductionPlan> deductionPlansWaitDeduct = deductionPlans.stream()
                .filter(item -> Objects.equals(item.getStatus(), DEDUCTION_PLAN_STATUS_INIT) || Objects.equals(item.getStatus(), DEDUCTION_PLAN_STATUS_FAIL))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deductionPlansWaitDeduct)) {
            log.warn("INSTALLMENT DEDUCT WARN! deductionPlansWaitDeduct is null. uid={}", installmentRecord.getUid());
            return Triple.of(false, "301053", "该期无可代扣的计划");
        }
        
        // 每次代扣都生成新的payNo，避免因为payNo一样导致问题
        InstallmentUtil.generatePayNo(installmentRecord.getUid(), deductionPlansWaitDeduct);
        
        try {
            for (InstallmentDeductionPlan deductionPlan : deductionPlansWaitDeduct) {
                String repaymentPlanNo = OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SIGN_AGREEMENT_PAY, installmentRecord.getUid());
                
                BigDecimal zeroAmount = new BigDecimal("0.00");
                
                // 生成代扣记录
                InstallmentDeductionRecord deductionRecord = new InstallmentDeductionRecord();
                deductionRecord.setUid(installmentRecord.getUid());
                deductionRecord.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
                deductionRecord.setPayNo(deductionPlan.getPayNo());
                deductionRecord.setRepaymentPlanNo(repaymentPlanNo);
                deductionRecord.setUserName(installmentRecord.getUserName());
                deductionRecord.setMobile(installmentRecord.getMobile());
                deductionRecord.setAmount(deductionPlan.getAmount());
                deductionRecord.setStatus(Objects.equals(deductionPlan.getAmount(), zeroAmount) ? DEDUCTION_RECORD_STATUS_SUCCESS : DEDUCTION_RECORD_STATUS_INIT);
                deductionRecord.setIssue(deductionPlan.getIssue());
                deductionRecord.setPlanId(deductionPlan.getId());
                deductionRecord.setSubject(null);
                deductionRecord.setTenantId(deductionPlan.getTenantId());
                deductionRecord.setFranchiseeId(deductionPlan.getFranchiseeId());
                deductionRecord.setCreateTime(System.currentTimeMillis());
                deductionRecord.setUpdateTime(System.currentTimeMillis());
                installmentDeductionRecordService.insert(deductionRecord);
                
                // 代扣0元，0元只有一个代扣计划，也只会出现在第一期，直接在0元处理中续费套餐就可以了，也不用考虑代扣完解约的问题
                if (Objects.equals(deductionPlan.getAmount(), zeroAmount)) {
                    handleDeductZero(installmentRecord, deductionPlan, deductionRecord);
                    return Triple.of(true, null, null);
                }
                
                // 生成请求对象
                FyCommonQuery<FyAgreementPayRequest> fyCommonQuery = generateAgreementPayQuery(deductionPlan, installmentRecord, fyConfig, repaymentPlanNo);
                
                String traceId = MDC.get(CommonConstant.TRACE_ID);
                inheritableThreadLocal.set(traceId);
                // 异步发起代扣
                initiatingDeductThreadPool.execute(() -> {
                    MDC.put(CommonConstant.TRACE_ID, inheritableThreadLocal.get());
                    try {
                        fyAgreementService.agreementPay(fyCommonQuery);
                    } catch (Exception e) {
                        log.error("DEDUCT FAIL! uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo(), e);
                    } finally {
                        MDC.clear();
                    }
                });
                
                // 发送延迟消息，1分钟后将代扣计划、代扣记录处理成失败状态
                InstallmentMqCommonDTO commonDTO = new InstallmentMqCommonDTO();
                commonDTO.setDeductionPlanId(deductionPlan.getId());
                commonDTO.setDeductionRecordId(deductionRecord.getId());
                commonDTO.setTraceId(traceId);
                rocketMqService.sendAsyncMsg(MqProducerConstant.INSTALLMENT_BUSINESS_TOPIC, JsonUtil.toJson(commonDTO), MqProducerConstant.INSTALLMENT_DEDUCT_FAIL_TAG, null, 5);
            }
            
            return Triple.of(true, "301052", "已发起代扣，请稍后查看代扣结果");
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR! uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo(), e);
        } finally {
            // 释放锁
            redisService.delete(String.format(CACHE_INSTALLMENT_DEDUCT_LOCK, installmentRecord.getUid()));
        }
        return Triple.of(false, "301006", "代扣失败");
    }
    
    @Override
    public R<String> createTerminatingRecord(String externalAgreementNo, String reason) {
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(externalAgreementNo);
        if (Objects.isNull(installmentRecord) || Arrays.asList(INSTALLMENT_RECORD_STATUS_COMPLETED, INSTALLMENT_RECORD_STATUS_CANCELLED).contains(installmentRecord.getStatus())) {
            log.info("CREATE TERMINATING RECORD INFO! Record cancellation externalAgreementNo={}", externalAgreementNo);
            return R.fail("301012", "分期套餐已解约");
        }
        
        Integer num = (Integer) eleRefundOrderService.queryCount(
                EleRefundQuery.builder().uid(installmentRecord.getUid()).statuses(Arrays.asList(EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUND)).build()).getData();
        if (Objects.nonNull(num) && num > 0) {
            return R.fail("301013", "有未完成的押金退款");
        }
        
        InstallmentTerminatingRecordQuery query = new InstallmentTerminatingRecordQuery();
        query.setUid(installmentRecord.getUid());
        query.setStatuses(List.of(TERMINATING_RECORD_STATUS_INIT));
        
        List<InstallmentTerminatingRecord> records = installmentTerminatingRecordService.listForRecordWithStatus(query);
        if (!CollectionUtils.isEmpty(records)) {
            return R.fail("301014", "有未完成的解约申请");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        recordQuery.setUid(installmentRecord.getUid());
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        InstallmentTerminatingRecord installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, reason, false);
        installmentTerminatingRecordService.insert(installmentTerminatingRecord);
        
        return R.ok();
    }
    
    @Override
    public void handleDeductZero(InstallmentRecord installmentRecord, InstallmentDeductionPlan deductionPlan, InstallmentDeductionRecord deductionRecord) {
        handleBatteryMemberCard(installmentRecord, deductionPlan, installmentRecord.getUid());
        
        InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
        deductionPlanUpdate.setId(deductionPlan.getId());
        deductionPlanUpdate.setPayNo(deductionRecord.getPayNo());
        deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_PAID);
        deductionPlanUpdate.setPaymentTime(System.currentTimeMillis());
        deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
        installmentDeductionPlanService.update(deductionPlanUpdate);
        
        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentRecordUpdate.setPaidInstallment(installmentRecord.getPaidInstallment() + 1);
        installmentRecordService.update(installmentRecordUpdate);
    }
    
    @Override
    public void terminateForReturnDeposit(Long uid) {
        InstallmentRecord installmentRecord = installmentRecordService.queryLatestUsingRecordByUid(uid);
        if (Objects.isNull(installmentRecord) || Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_TERMINATE)) {
            return;
        }
        
        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_INIT) || Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_UN_SIGN)) {
            installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_CANCEL_PAY);
            installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
            installmentRecordService.update(installmentRecordUpdate);
            return;
        }
        
        FyConfig config = fyConfigService.queryByTenantIdFromCache(installmentRecord.getTenantId());
        if (Objects.isNull(config)) {
            log.error("TERMINATING INSTALLMENT RECORD FOR RETURN DEPOSIT ERROR! no fyConfig, tenantId={}", installmentRecord.getTenantId());
        }
        
        InstallmentTerminatingRecordQuery query = new InstallmentTerminatingRecordQuery();
        query.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        query.setStatus(TERMINATING_RECORD_STATUS_INIT);
        List<InstallmentTerminatingRecord> records = installmentTerminatingRecordService.listForRecordWithStatus(query);
        
        // 根据解约记录做对应的处理
        InstallmentTerminatingRecord installmentTerminatingRecord;
        if (CollectionUtils.isEmpty(records)) {
            installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, "退押解约", false);
            installmentTerminatingRecord.setAuditorId(SecurityUtils.getUid());
            installmentTerminatingRecordService.insert(installmentTerminatingRecord);
        } else if (records.size() > 1) {
            log.error("TERMINATING INSTALLMENT RECORD FOR RETURN DEPOSIT ERROR! terminating record with init more than one. externalAgreementNo={}",
                    installmentRecord.getExternalAgreementNo());
            return;
        } else {
            installmentTerminatingRecord = records.get(0);
        }
        
        R<String> terminatingR = terminatingInstallmentRecord(installmentRecord, config);
        if (terminatingR.isSuccess()) {
            return;
        }
        
        InstallmentTerminatingRecord terminatingRecordUpdate = new InstallmentTerminatingRecord();
        terminatingRecordUpdate.setId(installmentTerminatingRecord.getId());
        terminatingRecordUpdate.setStatus(TERMINATING_RECORD_STATUS_REFUSE);
        terminatingRecordUpdate.setUpdateTime(System.currentTimeMillis());
        installmentTerminatingRecordService.update(terminatingRecordUpdate);
    }
    
    @Override
    public Triple<Boolean, String, Object> handleBatteryMemberCard(InstallmentRecord installmentRecord, InstallmentDeductionPlan deductionPlan, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        
        ElectricityMemberCardOrder memberCardOrder = electricityMemberCardOrderService.queryOrderByAgreementNoAndIssue(deductionPlan.getExternalAgreementNo(), 1);
        
        // 给用户绑定套餐
        if (Objects.equals(deductionPlan.getIssue(), 1)) {
            ElectricityMemberCardOrder memberCardOrderUpdate = new ElectricityMemberCardOrder();
            memberCardOrderUpdate.setId(memberCardOrder.getId());
            memberCardOrderUpdate.setValidDays(deductionPlan.getRentTime());
            memberCardOrderUpdate.setCreateTime(System.currentTimeMillis());
            memberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityMemberCardOrderService.updateByID(memberCardOrderUpdate);
            
            unionTradeOrderService.manageMemberCardOrderV2(memberCardOrder.getOrderId(), ElectricityTradeOrder.STATUS_SUCCESS, userInfo);
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
    
    @Override
    public R<String> terminatingInstallmentRecord(InstallmentRecord installmentRecord, FyConfig config) {
        try {
            FyCommonQuery<FyReleaseAgreementRequest> commonQuery = new FyCommonQuery<>();
            FyReleaseAgreementRequest request = new FyReleaseAgreementRequest();
            request.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
            request.setNotifyUrl(String.format(fengYunConfig.getInstallmentNotifyUrl(), installmentRecord.getUid()));
            
            commonQuery.setChannelCode(config.getChannelCode());
            commonQuery.setFlowNo(installmentRecord.getExternalAgreementNo() + System.currentTimeMillis());
            commonQuery.setFyRequest(request);
            
            FyResult<FyReleaseAgreementRsp> result = fyAgreementService.releaseAgreement(commonQuery);
            if (!Objects.equals(result.getCode(), FY_RESULT_CODE_SUCCESS)) {
                log.info("TERMINATING INSTALLMENT RECORD FAIL! result={}", result);
                return R.fail("301025", "解约失败，请联系管理员");
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
        return R.fail("301025", "解约失败，请联系管理员");
    }
    
    private FyCommonQuery<FyAgreementPayRequest> generateAgreementPayQuery(InstallmentDeductionPlan deductionPlan, InstallmentRecord installmentRecord, FyConfig fyConfig,
            String repaymentPlanNo) {
        FyCommonQuery<FyAgreementPayRequest> fyCommonQuery = new FyCommonQuery<>();
        FyAgreementPayRequest request = new FyAgreementPayRequest();
        
        request.setPayNo(deductionPlan.getPayNo());
        request.setAgreementNo(installmentRecord.getAgreementNo());
        request.setRepaymentPlanNo(repaymentPlanNo);
        request.setTotalAmount(deductionPlan.getAmount().multiply(new BigDecimal("100")).intValue());
        request.setSubject("分期套餐代扣支付");
        request.setNotifyUrl(String.format(fengYunConfig.getAgreementPayNotifyUrl(), installmentRecord.getUid()));
        request.setUserName(installmentRecord.getUserName());
        request.setMobile(installmentRecord.getMobile());
        request.setProvinceName("陕西省");
        request.setCityName("西安市");
        
        fyCommonQuery.setChannelCode(fyConfig.getChannelCode());
        fyCommonQuery.setFlowNo(repaymentPlanNo + System.currentTimeMillis());
        fyCommonQuery.setFyRequest(request);
        
        return fyCommonQuery;
    }
    
    private R<Object> queryInterfaceForInstallmentRecord(String externalAgreementNo, FyConfig fyConfig) {
        try {
            FyCommonQuery<FyQuerySignAgreementRequest> commonQuery = new FyCommonQuery<>();
            
            FyQuerySignAgreementRequest request = new FyQuerySignAgreementRequest();
            request.setExternalAgreementNo(externalAgreementNo);
            
            commonQuery.setChannelCode(fyConfig.getChannelCode());
            commonQuery.setFlowNo(externalAgreementNo + System.currentTimeMillis());
            commonQuery.setFyRequest(request);
            FyResult<FyQuerySignAgreementRsp> result = fyAgreementService.querySignAgreement(commonQuery);
            if (Objects.equals(result.getCode(), FY_RESULT_CODE_SUCCESS)) {
                return R.ok(result.getFyResponse());
            }
        } catch (Exception e) {
            log.error("QUERY INSTALLMENT RECORD STATUS ERROR!", e);
        }
        return R.fail("查询失败");
    }
    
    private R<Object> queryInterfaceForDeductionRecord(String payNo, FyConfig fyConfig, String externalAgreementNo) {
        try {
            FyCommonQuery<FyQueryAgreementPayRequest> commonQuery = new FyCommonQuery<>();
            
            FyQueryAgreementPayRequest request = new FyQueryAgreementPayRequest();
            request.setPayNo(payNo);
            
            commonQuery.setChannelCode(fyConfig.getChannelCode());
            commonQuery.setFlowNo(externalAgreementNo + System.currentTimeMillis());
            commonQuery.setFyRequest(request);
            FyResult<FyQueryAgreementPayRsp> result = fyAgreementService.queryAgreementPay(commonQuery);
            
            if (Objects.equals(result.getCode(), FY_RESULT_CODE_SUCCESS)) {
                // 传递结果给外部方法校验
                return R.ok(result.getFyResponse());
            }
        } catch (Exception e) {
            log.error("QUERY INSTALLMENT RECORD STATUS ERROR!", e);
        }
        return R.fail("查询失败");
    }
    

}
