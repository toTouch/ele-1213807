package com.xiliulou.electricity.service.impl.installment;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.installment.InstallmentDeductionRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductNotifyQuery;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionRecordVO;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQueryAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyAgreementPayRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQueryAgreementPayRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyResult;
import com.xiliulou.pay.deposit.fengyun.service.FyAgreementService;
import com.xiliulou.pay.deposit.fengyun.utils.FyAesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_DEDUCT_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.*;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:56
 */
@Service
@Slf4j
@AllArgsConstructor
public class InstallmentDeductionRecordServiceImpl implements InstallmentDeductionRecordService {

    private InstallmentDeductionRecordMapper installmentDeductionRecordMapper;

    private FranchiseeService franchiseeService;

    private InstallmentRecordService installmentRecordService;

    private InstallmentDeductionPlanService installmentDeductionPlanService;

    private FyAgreementService fyAgreementService;

    private FengYunConfig fengYunConfig;

    private FyConfigService fyConfigService;

    private TenantService tenantService;

    private RedisService redisService;

    private UnionTradeOrderService unionTradeOrderService;

    private ElectricityMemberCardOrderService electricityMemberCardOrderService;

    private UserInfoService userInfoService;

    private ApplicationContext applicationContext;

    private BatteryMemberCardService batteryMemberCardService;

    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    private InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    @Override
    public Integer insert(InstallmentDeductionRecord installmentDeductionRecord) {
        return installmentDeductionRecordMapper.insert(installmentDeductionRecord);
    }

    @Override
    public Integer update(InstallmentDeductionRecord installmentDeductionRecord) {
        return installmentDeductionRecordMapper.update(installmentDeductionRecord);
    }

    @Slave
    @Override
    public R<List<InstallmentDeductionRecordVO>> listForPage(InstallmentDeductionRecordQuery installmentDeductionRecordQuery) {
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordMapper.selectPage(installmentDeductionRecordQuery);

        List<InstallmentDeductionRecordVO> collect = installmentDeductionRecords.parallelStream().map(installmentDeductionRecord -> {
            InstallmentDeductionRecordVO recordVO = new InstallmentDeductionRecordVO();
            BeanUtils.copyProperties(installmentDeductionRecord, recordVO);

            recordVO.setFranchiseeName(franchiseeService.queryByIdFromCache(installmentDeductionRecord.getFranchiseeId()).getName());
            return recordVO;
        }).collect(Collectors.toList());
        return R.ok(collect);
    }

    @Slave
    @Override
    public R<Integer> count(InstallmentDeductionRecordQuery installmentDeductionRecordQuery) {
        return R.ok(installmentDeductionRecordMapper.count(installmentDeductionRecordQuery));
    }

    @Override
    public R<String> deduct(String externalAgreementNo) {
        try {
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(externalAgreementNo);
            if (Objects.isNull(installmentRecord)) {
                return R.fail("301005", "签约记录不存在");
            }

            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.warn("INSTALLMENT DEDUCT WARN! The user is not associated with a tenant. uid={}", installmentRecord.getUid());
                return R.fail("301004", "请购买分期套餐后，再签约");
            }

            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(tenant.getId());
            if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
                return R.fail("301003", "签约代扣功能未配置相关信息！请联系客服处理");
            }

            // 查询出签约记录对应的代扣计划
            List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(
                    InstallmentDeductionPlanQuery.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo()).build()).getData();

            // 发起代扣
            Triple<Boolean, String, Object> initiatingDeductTriple = null;
            for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
                if (Objects.equals(deductionPlan.getIssue(), (installmentRecord.getPaidInstallment() + 1)) && deductionPlan.getDeductTime() <= System.currentTimeMillis()) {
                    initiatingDeductTriple = initiatingDeduct(deductionPlan, installmentRecord, fyConfig);
                }
            }

            if (Objects.nonNull(initiatingDeductTriple)) {
                return initiatingDeductTriple.getLeft() ? R.ok() : R.fail(initiatingDeductTriple.getMiddle());
            }
            return R.fail("301007", "无可代扣分期订单");
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR!", e);
        }
        return R.fail("301006", "代扣失败");
    }

    @Override
    public Triple<Boolean, String, Object> initiatingDeduct(InstallmentDeductionPlan deductionPlan, InstallmentRecord installmentRecord, FyConfig fyConfig) {
        if (redisService.setNx(String.format(CACHE_INSTALLMENT_DEDUCT_LOCK, installmentRecord.getUid()), "1", 3 * 1000L, false)) {
            return Triple.of(false, "已对该用户执行代扣，请稍候再试", null);
        }

        // payNo仅有20个字符，用uid加时间秒值不会重复
        String payNo = String.format("%08d", installmentRecord.getUid()) + (System.currentTimeMillis() / 1000);
        String repaymentPlanNo = OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SIGN_AGREEMENT_PAY, installmentRecord.getUid());

        // 生成代扣记录
        InstallmentDeductionRecord installmentDeductionRecord = new InstallmentDeductionRecord();
        installmentDeductionRecord.setUid(installmentDeductionRecord.getUid());
        installmentDeductionRecord.setExternalAgreementNo(installmentDeductionRecord.getExternalAgreementNo());
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

        try {

            FyCommonQuery<FyAgreementPayRequest> fyCommonQuery = new FyCommonQuery<>();

            FyAgreementPayRequest request = new FyAgreementPayRequest();

            request.setPayNo(payNo);
            request.setAgreementNo(deductionPlan.getExternalAgreementNo());
            request.setRepaymentPlanNo(repaymentPlanNo);
            request.setTotalAmount(Integer.valueOf(deductionPlan.getAmount().multiply(new BigDecimal("100")).toString()));
            request.setSubject("分期套餐代扣支付");
            request.setNotifyUrl(String.format(fengYunConfig.getAgreementPayNotifyUrl(), installmentRecord.getUid()));
            request.setUserName(installmentRecord.getUserName());
            request.setMobile(installmentRecord.getMobile());
            request.setProvinceName("陕西省");
            request.setCityName("西安市");

            fyCommonQuery.setChannelCode(fyConfig.getChannelCode());
            fyCommonQuery.setFlowNo(repaymentPlanNo + System.currentTimeMillis());
            fyCommonQuery.setFyRequest(request);
            FyResult<FyAgreementPayRsp> fyAgreementPayRspFyResult = fyAgreementService.agreementPay(fyCommonQuery);

            // 调用成功，则保存代扣中的记录
            if (Objects.equals(FY_SUCCESS_CODE, fyAgreementPayRspFyResult.getCode())) {

                installmentDeductionRecordMapper.insert(installmentDeductionRecord);
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
        installmentDeductionRecord.setStatus(DEDUCTION_RECORD_STATUS_FAIL);
        installmentDeductionRecordMapper.insert(installmentDeductionRecord);
        return Triple.of(false, "代扣失败", null);
    }

    @Override
    public String agreementPayNotify(String bizContent, Long uid) {
        try {
            String decrypt = FyAesUtil.decrypt(bizContent, fengYunConfig.getAesKey());
            InstallmentDeductNotifyQuery deductNotifyQuery = JsonUtil.fromJson(decrypt, InstallmentDeductNotifyQuery.class);
            InstallmentDeductionRecord deductionRecord = installmentDeductionRecordMapper.selectRecordByPayNo(deductNotifyQuery.getPayNo());

            // 处理代扣成功的场景
            handleAgreementPaySuccess(deductionRecord);

            return "SUCCESS";
        } catch (Exception e) {
            log.error("NOTIFY AGREEMENT PAY ERROR!", e);
        }
        return null;
    }

    @Override
    public InstallmentDeductionRecord queryByPayNo(String payNo) {
        return installmentDeductionRecordMapper.selectRecordByPayNo(payNo);
    }

    @Override
    public void dailyInstallmentDeduct() {
        List<String> externalAgreementNos = installmentDeductionPlanService.listExternalAgreementNoForDeduct(System.currentTimeMillis());

        externalAgreementNos.parallelStream().forEach(externalAgreementNo -> {
            InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryPlanForDeductByAgreementNo(externalAgreementNo);

            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(deductionPlan.getTenantId());

            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(externalAgreementNo);

            initiatingDeduct(deductionPlan, installmentRecord, fyConfig);
        });
    }

    @Override
    public R queryStatus(String payNo) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(fyConfig)) {
            return R.fail("签约代扣功能未配置相关信息！请联系客服处理");
        }

        InstallmentDeductionRecord installmentDeductionRecord = installmentDeductionRecordMapper.selectRecordByPayNo(payNo);

        // 调用蜂云接口查询结果
        R queried = queryInterfaceForDeductionRecord(payNo, fyConfig, installmentDeductionRecord.getExternalAgreementNo());
        if (!queried.isSuccess()) {
            return queried;
        }

        FyQueryAgreementPayRsp rsp = (FyQueryAgreementPayRsp)queried.getData();
        if (Objects.equals(rsp.getStatus(), AGREEMENT_PAY_QUERY_STATUS_SUCCESS)) {
            // 处理成功的场景
            handleAgreementPaySuccess(installmentDeductionRecord);
        }
        return R.ok();
    }

    public R queryInterfaceForDeductionRecord(String payNo, FyConfig fyConfig, String externalAgreementNo) {
        try {
            FyCommonQuery<FyQueryAgreementPayRequest> commonQuery = new FyCommonQuery<>();

            FyQueryAgreementPayRequest request = new FyQueryAgreementPayRequest();
            request.setPayNo(payNo);

            commonQuery.setChannelCode(fyConfig.getChannelCode());
            commonQuery.setFlowNo(externalAgreementNo + System.currentTimeMillis());
            commonQuery.setFyRequest(request);
            FyResult<FyQueryAgreementPayRsp> result = fyAgreementService.queryAgreementPay(commonQuery);
            if (!Objects.equals(result.getCode(), FY_SUCCESS_CODE)) {
                return R.ok(result.getFyResponse());
            }
        } catch (Exception e) {
            log.error("QUERY INSTALLMENT RECORD STATUS ERROR!", e);
        }
        return R.fail("查询失败");
    }

    public R handleAgreementPaySuccess(InstallmentDeductionRecord deductionRecord) {
        if (Objects.equals(deductionRecord.getStatus(), DEDUCTION_RECORD_STATUS_SUCCESS)) {
            return R.ok();
        }

        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(deductionRecord.getExternalAgreementNo());
        
        InstallmentDeductionPlan deductionPlan = installmentDeductionPlanService.queryByAgreementNoAndIssue(deductionRecord.getExternalAgreementNo(), deductionRecord.getIssue());
        
        Triple<Boolean, String, Object> handlePackageTriple = null;
        if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
            // 处理换电代扣成功的场景
            handlePackageTriple = applicationContext.getBean(InstallmentDeductionRecordServiceImpl.class).handleBatteryMemberCard(deductionRecord, installmentRecord, deductionRecord.getUid());
        }

        // 代扣成功后其他记录的处理
        if (Objects.nonNull(handlePackageTriple) && handlePackageTriple.getLeft()) {
            InstallmentDeductionPlan deductionPlanUpdate = new InstallmentDeductionPlan();
            deductionPlanUpdate.setId(deductionPlan.getId());
            deductionPlanUpdate.setPayNo(deductionRecord.getPayNo());
            deductionPlanUpdate.setStatus(DEDUCTION_PLAN_STATUS_PAID);
            deductionPlanUpdate.setPaymentTime(System.currentTimeMillis());
            deductionPlanUpdate.setUpdateTime(System.currentTimeMillis());
            installmentDeductionPlanService.update(deductionPlanUpdate);
            
            InstallmentDeductionRecord deductionRecordUpdate = new InstallmentDeductionRecord();
            deductionRecordUpdate.setId(deductionRecord.getId());
            deductionRecordUpdate.setStatus(DEDUCTION_RECORD_STATUS_SUCCESS);
            deductionRecordUpdate.setUpdateTime(System.currentTimeMillis());
            installmentDeductionRecordMapper.update(deductionRecordUpdate);

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
                installmentTerminatingRecordService.terminatingInstallmentRecord(installmentRecord);
            }
        }
        return R.ok();
    }

    private Triple<Boolean, String, Object> handleBatteryMemberCard(InstallmentDeductionRecord deductionRecord, InstallmentRecord installmentRecord, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId());

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);

        ElectricityMemberCardOrder memberCardOrder = electricityMemberCardOrderService.queryOrderByAgreementNoAndIssue(deductionRecord.getExternalAgreementNo(), 1);

        // 优先给用户绑定套餐
        if (Objects.equals(deductionRecord.getIssue(), 1)) {
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
}
