package com.xiliulou.electricity.service.impl.installment;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignNotifyQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQuerySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.Vars;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQuerySignAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyResult;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FySignAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.service.FyAgreementService;
import com.xiliulou.pay.deposit.fengyun.utils.FyAesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_FORM_BODY;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_SIGN_NOTIFY_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.CHANNEL_FROM_H5;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_CANCEL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.FY_SUCCESS_CODE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_CANCELLED;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_TERMINATE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UN_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.NOTIFY_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.SIGN_QUERY_STATUS_CANCEL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.SIGN_QUERY_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_INIT;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:51
 */
@Service
@Slf4j
public class InstallmentRecordServiceImpl implements InstallmentRecordService {
    
    @Autowired
    private InstallmentRecordMapper installmentRecordMapper;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private FyAgreementService fyAgreementService;
    
    @Autowired
    private FengYunConfig fengYunConfig;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private FyConfigService fyConfigService;
    
    @Autowired
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @Autowired
    private InstallmentDeductionRecordService installmentDeductionRecordService;
    
    @Autowired
    private InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    XllThreadPoolExecutorService initiatingDeductThreadPool = XllThreadPoolExecutors.newFixedThreadPool("INSTALLMENT_INITIATING_DEDUCT", 1, "initiatingDeduct");
    
    @Override
    public Integer insert(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.insert(installmentRecord);
    }
    
    @Override
    public Integer update(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.update(installmentRecord);
    }
    
    @Slave
    @Override
    public R<List<InstallmentRecordVO>> listForPage(InstallmentRecordQuery installmentRecordQuery) {
        List<InstallmentRecord> installmentRecords = installmentRecordMapper.selectPage(installmentRecordQuery);
        
        List<InstallmentRecordVO> installmentRecordVos = installmentRecords.parallelStream().map(installmentRecord -> {
            InstallmentRecordVO installmentRecordVO = new InstallmentRecordVO();
            BeanUtils.copyProperties(installmentRecord, installmentRecordVO);
            
            // 设置加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(installmentRecord.getFranchiseeId());
            installmentRecordVO.setFranchiseeName(franchisee.getName());
            
            // 设置套餐信息
            setPackageMessage(installmentRecordVO, installmentRecord);
            
            installmentRecordVO.setUnpaidInstallmentNo(installmentRecordVO.getInstallmentNo() - installmentRecordVO.getPaidInstallment());
            return installmentRecordVO;
        }).collect(Collectors.toList());
        
        return R.ok(installmentRecordVos);
    }
    
    @Slave
    @Override
    public R<Integer> count(InstallmentRecordQuery installmentRecordQuery) {
        return R.ok(installmentRecordMapper.count(installmentRecordQuery));
    }
    
    @Override
    public Triple<Boolean, String, InstallmentRecord> generateInstallmentRecord(InstallmentPayQuery query, BatteryMemberCard batteryMemberCard,
            CarRentalPackagePo carRentalPackagePo, UserInfo userInfo) {
        // 生成分期签约记录订单号
        String externalAgreementNo = OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SIGN, userInfo.getUid());
        InstallmentRecord installmentRecord = InstallmentRecord.builder().uid(userInfo.getUid()).externalAgreementNo(externalAgreementNo).userName(null).mobile(null)
                .packageType(query.getPackageType()).status(INSTALLMENT_RECORD_STATUS_INIT).paidInstallment(0).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        
        if (InstallmentConstants.PACKAGE_TYPE_BATTERY.equals(query.getPackageType())) {
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, null, null);
            }
            
            Integer installmentNo = batteryMemberCard.getValidDays() / 30;
            installmentRecord.setInstallmentNo(installmentNo);
            installmentRecord.setTenantId(batteryMemberCard.getTenantId());
            installmentRecord.setFranchiseeId(batteryMemberCard.getFranchiseeId());
            installmentRecord.setPackageId(batteryMemberCard.getId());
        }
        return Triple.of(true, null, installmentRecord);
    }
    
    @Override
    public R<String> sign(InstallmentSignQuery query, HttpServletRequest request) {
        Long uid = null;
        try {
            uid = SecurityUtils.getUid();
            InstallmentRecord installmentRecord = queryRecordWithStatusForUser(uid, Arrays.asList(INSTALLMENT_RECORD_STATUS_INIT, INSTALLMENT_RECORD_STATUS_UN_SIGN));
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
                return R.fail("初次签约需要输入用户信息");
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
                InstallmentRecord installmentRecordUpdate = InstallmentRecord.builder().id(installmentRecord.getId()).status(INSTALLMENT_RECORD_STATUS_UN_SIGN)
                        .userName(query.getUserName()).mobile(query.getMobile()).updateTime(System.currentTimeMillis()).build();
                applicationContext.getBean(InstallmentRecordServiceImpl.class).update(installmentRecordUpdate);
                // 二维码缓存2天零23小时50分钟，减少卡在二维码3天有效期的末尾的出错
                redisService.saveWithString(String.format(CACHE_INSTALLMENT_FORM_BODY, uid), fySignResult.getFyResponse().getFormBody(), Long.valueOf(2 * 24 * 60 + 23 * 60 + 50),
                        TimeUnit.MINUTES);
                return R.ok(fySignResult.getFyResponse().getFormBody());
            }
        } catch (Exception e) {
            log.error("INSTALLMENT SIGN ERROR! uid={}", uid, e);
            throw new BizException("签约失败，请联系管理员");
        }
        return R.fail("301002", "签约失败，请联系管理员");
    }
    
    @Override
    public InstallmentRecord queryRecordWithStatusForUser(Long uid, List<Integer> statuses) {
        return installmentRecordMapper.selectRecordWithStatusForUser(uid, statuses);
    }
    
    @Override
    public String signNotify(String bizContent, Long uid) {
        try {
            String decrypt = FyAesUtil.decrypt(bizContent, fengYunConfig.getAesKey());
            InstallmentSignNotifyQuery signNotifyQuery = JsonUtil.fromJson(decrypt, InstallmentSignNotifyQuery.class);
            
            InstallmentRecord installmentRecord = applicationContext.getBean(InstallmentRecordService.class).queryByExternalAgreementNo(signNotifyQuery.getExternalAgreementNo());
            
            if (NOTIFY_STATUS_SIGN.equals(Integer.valueOf(signNotifyQuery.getStatus()))) {
                handleSign(installmentRecord);
            } else {
                // 处理解约成功回调
                handleTerminating(installmentRecord);
            }
            
            return "SUCCESS";
        } catch (Exception e) {
            log.error("INSTALLMENT NOTIFY ERROR! uid={}, bizContent={}", uid, bizContent, e);
            return null;
        }
    }
    
    @Override
    public InstallmentRecord queryByExternalAgreementNo(String externalAgreementNo) {
        return installmentRecordMapper.selectByExternalAgreementNo(externalAgreementNo);
    }
    
    @Slave
    @Override
    public R<InstallmentRecordVO> queryInstallmentRecordForUser() {
        Long uid = SecurityUtils.getUid();
        InstallmentRecord installmentRecord = installmentRecordMapper.selectRecordWithStatusForUser(uid,
                Arrays.asList(INSTALLMENT_RECORD_STATUS_INIT, INSTALLMENT_RECORD_STATUS_UN_SIGN, INSTALLMENT_RECORD_STATUS_SIGN, INSTALLMENT_RECORD_STATUS_TERMINATE));
        
        InstallmentRecordVO installmentRecordVO = new InstallmentRecordVO();
        BeanUtils.copyProperties(installmentRecord, installmentRecordVO);
        // 设置套餐信息
        setPackageMessage(installmentRecordVO, installmentRecord);
        
        // 查询有无逾期代扣计划
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(
                InstallmentDeductionPlanQuery.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo())
                        .statuses(Arrays.asList(DEDUCTION_PLAN_STATUS_INIT, DEDUCTION_PLAN_STATUS_FAIL)).endTime(System.currentTimeMillis()).build()).getData();
        installmentRecordVO.setOverdue(CollectionUtils.isEmpty(deductionPlans) ? 0 : 1);
        
        // 查询有无审核中的解约申请
        List<InstallmentTerminatingRecord> terminatingRecords = installmentTerminatingRecordService.listForRecordWithStatus(
                InstallmentTerminatingRecordQuery.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo()).statuses(List.of(TERMINATING_RECORD_STATUS_INIT))
                        .build());
        installmentRecordVO.setUnderReview(CollectionUtils.isEmpty(terminatingRecords) ? 0 : 1);
        
        return R.ok(installmentRecordVO);
    }
    
    @Override
    public R queryStatus(String externalAgreementNo) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(fyConfig)) {
            return R.fail("签约代扣功能未配置相关信息！请联系客服处理");
        }
        
        R queried = queryInterfaceForInstallmentRecord(externalAgreementNo, fyConfig);
        if (!queried.isSuccess()) {
            return queried;
        }
        
        InstallmentRecord installmentRecord = installmentRecordMapper.selectByExternalAgreementNo(externalAgreementNo);
        FyQuerySignAgreementRsp rsp = (FyQuerySignAgreementRsp) queried.getData();
        
        if (Objects.equals(Integer.valueOf(rsp.getStatus()), SIGN_QUERY_STATUS_SIGN)) {
            handleSign(installmentRecord);
        } else if (Objects.equals(Integer.valueOf(rsp.getStatus()), SIGN_QUERY_STATUS_CANCEL)) {
            handleTerminating(installmentRecord);
        }
        
        return R.ok();
    }
    
    @Override
    public R terminateRecord(String externalAgreementNo) {
        InstallmentRecord installmentRecord = installmentRecordMapper.selectByExternalAgreementNo(externalAgreementNo);
        if (Objects.isNull(installmentRecord)) {
            return R.fail("签约记录为空");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(installmentRecord.getUid());
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return R.fail("未退还电池");
        }
        
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(
                InstallmentDeductionPlanQuery.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo()).status(DEDUCTION_RECORD_STATUS_INIT).build()).getData();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(deductionPlans)) {
            return R.fail("当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        InstallmentTerminatingRecord installmentTerminatingRecord = installmentTerminatingRecordService.generateTerminatingRecord(installmentRecord, null);
        installmentTerminatingRecordService.insert(installmentTerminatingRecord);
        installmentTerminatingRecordService.terminatingInstallmentRecord(installmentRecord);
        
        return R.ok();
    }
    
    
    public void signNotifySaveAndUpdate(InstallmentRecord installmentRecordUpdate, List<InstallmentDeductionPlan> deductionPlanTriple) {
        // 更新签约记录
        applicationContext.getBean(InstallmentRecordService.class).update(installmentRecordUpdate);
        
        deductionPlanTriple.forEach(deductionPlan -> {
            installmentDeductionPlanService.insert(deductionPlan);
        });
        R.ok();
    }
    
    private void setPackageMessage(InstallmentRecordVO installmentRecordVO, InstallmentRecord installmentRecord) {
        if (Objects.equals(installmentRecordVO.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecordVO.getPackageId());
            
            installmentRecordVO.setPackageName(batteryMemberCard.getName());
            installmentRecordVO.setInstallmentServiceFee(batteryMemberCard.getInstallmentServiceFee());
            installmentRecordVO.setDownPayment(batteryMemberCard.getDownPayment());
            installmentRecordVO.setRentPrice(batteryMemberCard.getRentPrice());
            
            // 计算剩余每期金额
            installmentRecordVO.setRemainingPrice(InstallmentUtil.calculateSuborderAmount(2, installmentRecord, batteryMemberCard));
        } else {
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(installmentRecordVO.getPackageId());
            installmentRecordVO.setPackageName(carRentalPackagePo.getName());
        }
    }
    
    private R<String> handleSign(InstallmentRecord installmentRecord) {
        if (Objects.isNull(installmentRecord) || !INSTALLMENT_RECORD_STATUS_UN_SIGN.equals(installmentRecord.getStatus())) {
            log.warn("SIGN NOTIFY WARN! no right installmentRecord, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
        }
        
        if (redisService.setNx(String.format(CACHE_INSTALLMENT_SIGN_NOTIFY_LOCK, installmentRecord.getUid()), "1", 3 * 1000L, false)) {
            return R.ok();
        }
        
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_SIGN)) {
            return R.ok();
        }
        
        // 更新签约记录状态
        InstallmentRecord installmentRecordUpdate = InstallmentRecord.builder().id(installmentRecord.getId()).status(INSTALLMENT_RECORD_STATUS_SIGN)
                .updateTime(System.currentTimeMillis()).build();
        
        // 生成还款计划
        List<InstallmentDeductionPlan> deductionPlanList = installmentDeductionPlanService.generateDeductionPlan(installmentRecord);
        if (Objects.isNull(deductionPlanList)) {
            log.warn("SIGN NOTIFY WARN! generate deduction plan, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
        }
        
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
            log.warn("SIGN NOTIFY WARN! initiating deduct fail, uid={}, externalAgreementNo={}", installmentRecord.getUid(), installmentRecord.getExternalAgreementNo());
        }
        
        // 尽快给用户完成代扣和套餐绑定，异步发起代扣
        initiatingDeductThreadPool.execute(() -> {
            installmentDeductionRecordService.initiatingDeduct(deductionPlanList.get(0), installmentRecord, fyConfig);
        });
        
        // 更新或保存入数据库
        applicationContext.getBean(InstallmentRecordServiceImpl.class).signNotifySaveAndUpdate(installmentRecordUpdate, deductionPlanList);
        
        // 签约成功，删除签约二维码缓存
        redisService.delete(String.format(CACHE_INSTALLMENT_FORM_BODY, installmentRecord.getUid()));
        return R.ok();
    }
    
    private R<String> handleTerminating(InstallmentRecord installmentRecord) {
        if (Objects.equals(installmentRecord.getStatus(), INSTALLMENT_RECORD_STATUS_CANCELLED)) {
            return R.ok();
        }
        
        // 更新签约记录
        InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
        installmentRecordUpdate.setId(installmentRecord.getId());
        installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_CANCELLED);
        installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
        
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(
                InstallmentDeductionPlanQuery.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo()).status(DEDUCTION_PLAN_STATUS_INIT).build()).getData();
        
        installmentRecordMapper.update(installmentRecordUpdate);
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
    
    public R queryInterfaceForInstallmentRecord(String externalAgreementNo, FyConfig fyConfig) {
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
}
