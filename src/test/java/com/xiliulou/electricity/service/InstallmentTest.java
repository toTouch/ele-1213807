package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.utils.InstallmentUtil;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/5 16:41
 */
public class InstallmentTest {

    @Test
    public void testGeneratePlan() {
        InstallmentRecord installmentRecord = new InstallmentRecord();
        installmentRecord.setPackageType(0);
        installmentRecord.setInstallmentNo(2);
        
        InstallmentDeductionPlan basicDeductionPlan = new InstallmentDeductionPlan();
        basicDeductionPlan.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        basicDeductionPlan.setPackageId(installmentRecord.getPackageId());
        basicDeductionPlan.setPackageType(installmentRecord.getPackageType());
        basicDeductionPlan.setStatus(DEDUCTION_PLAN_STATUS_INIT);
        basicDeductionPlan.setTenantId(installmentRecord.getTenantId());
        basicDeductionPlan.setFranchiseeId(installmentRecord.getFranchiseeId());
        basicDeductionPlan.setCreateTime(System.currentTimeMillis());
        basicDeductionPlan.setUpdateTime(System.currentTimeMillis());
        
        // 获取套餐
        BatteryMemberCard batteryMemberCard = new BatteryMemberCard();
        batteryMemberCard.setDownPayment(new BigDecimal("0.03"));
        batteryMemberCard.setRentPrice(new BigDecimal("0.06"));
        batteryMemberCard.setValidDays(60);
        
        List<InstallmentDeductionPlan> planList;
        
        BigDecimal deductionMaxAmount = new BigDecimal("0.02");
        
        planList = new ArrayList<>(installmentRecord.getInstallmentNo());
        for (int i = 1; i <= installmentRecord.getInstallmentNo(); i++) {
            BigDecimal suborderAmount = InstallmentUtil.calculateSuborderAmount(i, installmentRecord, batteryMemberCard);
            
            // 生成每一期内所有代扣计划的相同数据
            InstallmentDeductionPlan deductionPlan = new InstallmentDeductionPlan();
            BeanUtils.copyProperties(basicDeductionPlan, deductionPlan);
            deductionPlan.setIssue(i);
            deductionPlan.setRentTime(InstallmentUtil.calculateSuborderRentTime(i, installmentRecord, batteryMemberCard));
            deductionPlan.setDeductTime(InstallmentUtil.calculateSuborderDeductTime(i));
            
            if (suborderAmount.compareTo(deductionMaxAmount) <= 0) {
                // 单期金额在最大单笔代扣金额内，不拆分
                deductionPlan.setAmount(suborderAmount);
                planList.add(deductionPlan);
            } else {
                // 单期金额过大，需要拆分
                // 求金额为最大代扣金额的子订单的数量
                BigDecimal suborderNumber = suborderAmount.divide(deductionMaxAmount, 0, RoundingMode.DOWN);
                
                // 求剩余需代扣金额
                BigDecimal remainingAmount = suborderAmount.subtract(deductionMaxAmount.multiply(suborderNumber));
                
                // 首先生成剩余金额的代扣计划
                deductionPlan.setAmount(remainingAmount);
                planList.add(deductionPlan);
                
                // 生成金额为单笔最大金额的代扣计划
                for (int i1 = 0; i1 < suborderNumber.intValue(); i1++) {
                    InstallmentDeductionPlan suborderDeductionPlan = new InstallmentDeductionPlan();
                    BeanUtils.copyProperties(deductionPlan, suborderDeductionPlan);
                    suborderDeductionPlan.setAmount(deductionMaxAmount);
                    planList.add(suborderDeductionPlan);
                }
            }
        }
        
        List<BigDecimal> collect = planList.stream().map(InstallmentDeductionPlan::getAmount).collect(Collectors.toList());
        
        System.out.println(collect);
    }
}
