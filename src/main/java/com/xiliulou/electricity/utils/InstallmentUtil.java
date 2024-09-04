package com.xiliulou.electricity.utils;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/4 9:29
 */
@AllArgsConstructor
public class InstallmentUtil {
    
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    /**
     * 计算还款计划每期金额
     */
    public static BigDecimal calculateSuborderAmount(InstallmentRecord installmentRecord, BatteryMemberCard memberCard) {
        BigDecimal payAmount;
        if (Objects.equals(installmentRecord.getPaidInstallment(), 0)) {
            // 首期金额
            payAmount = memberCard.getDownPayment();
        } else {
            // 第二期~倒数第二期的金额
            BigDecimal otherInstallmentNo = new BigDecimal(String.valueOf(installmentRecord.getInstallmentNo() - 1));
            payAmount = memberCard.getRentPrice().subtract(memberCard.getDownPayment()).divide(otherInstallmentNo, 2, RoundingMode.DOWN);
            
            if (installmentRecord.getInstallmentNo() - installmentRecord.getPaidInstallment() == 1) {
                // 保证最后一期金额不出错，用总金额减去其他金额得到
                BigDecimal tempPayAmount = payAmount;
                BigDecimal middleInstallmentNo = new BigDecimal(String.valueOf(installmentRecord.getInstallmentNo() - 2));
                payAmount = memberCard.getRentPrice().subtract(memberCard.getDownPayment()).subtract(tempPayAmount.multiply(middleInstallmentNo));
            }
        }
        
        return payAmount;
    }
    

}
