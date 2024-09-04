package com.xiliulou.electricity.service.impl.installment;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.installment.InstallmentDeductionRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionRecordVO;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.service.FyAgreementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                    InstallmentRecordQuery.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo()).build()).getData();
            
            // 发起代扣
            Triple<Boolean, Object, Object> initiatingDeductTriple = null;
            for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
                if (Objects.equals(deductionPlan.getIssue(), (installmentRecord.getPaidInstallment() + 1))) {
                    initiatingDeductTriple = initiatingDeduct(deductionPlan, installmentRecord, fyConfig);
                }
            }
            
            if (Objects.nonNull(initiatingDeductTriple) && initiatingDeductTriple.getLeft()) {
                return R.ok();
            }
            
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR!", e);
        }
        return R.fail("301006", "代扣失败");
    }
    
    @Override
    public Triple<Boolean, Object, Object> initiatingDeduct(InstallmentDeductionPlan deductionPlan, InstallmentRecord installmentRecord, FyConfig fyConfig) {
        try {
            FyCommonQuery<FyAgreementPayRequest> fyCommonQuery = new FyCommonQuery<>();
            
            FyAgreementPayRequest request = new FyAgreementPayRequest();
            // payNo仅有20个字符，用uid加时间秒值不会重复
            String payNo = String.format("%08d", installmentRecord.getUid()) + (System.currentTimeMillis() / 1000);
            String repaymentPlanNo = OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SIGN_AGREEMENT_PAY, installmentRecord.getUid());
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
            fyCommonQuery.setFlowNo(repaymentPlanNo);
            fyCommonQuery.setFyRequest(request);
            
            fyAgreementService.agreementPay(fyCommonQuery);
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR!", e);
            return Triple.of(false, null, null);
        }
        return Triple.of(true, null, null);
    }
}
