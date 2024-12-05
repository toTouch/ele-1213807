package com.xiliulou.electricity.utils;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PACKAGE_TYPE_BATTERY;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/4 9:29
 */
public class InstallmentUtil {
    
    /**
     * 计算还款计划每期金额
     */
    public static BigDecimal calculateSuborderAmount(Integer issue, InstallmentRecord installmentRecord, BatteryMemberCard memberCard) {
        BigDecimal payAmount = null;
        if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
            if (Objects.equals(issue, 1)) {
                // 首期金额
                payAmount = memberCard.getDownPayment();
            } else {
                if (issue == 2 && 2 == installmentRecord.getInstallmentNo()) {
                    return memberCard.getRentPrice().subtract(memberCard.getDownPayment());
                }
                // 第二期~倒数第二期的金额
                BigDecimal otherInstallmentNo = new BigDecimal(String.valueOf(installmentRecord.getInstallmentNo() - 1));
                payAmount = memberCard.getRentPrice().subtract(memberCard.getDownPayment()).divide(otherInstallmentNo, 2, RoundingMode.DOWN);
                
                if (Objects.equals(issue, installmentRecord.getInstallmentNo())) {
                    // 保证最后一期金额不出错，用总金额减去其他金额得到
                    BigDecimal tempPayAmount = payAmount;
                    BigDecimal middleInstallmentNo = new BigDecimal(String.valueOf(installmentRecord.getInstallmentNo() - 2));
                    payAmount = memberCard.getRentPrice().subtract(memberCard.getDownPayment()).subtract(tempPayAmount.multiply(middleInstallmentNo));
                }
            }
        }
        return payAmount;
    }
    
    /**
     * 计算还款计划每期租期，用于在签约完成后生成还款计划
     */
    public static Integer calculateSuborderRentTime(Integer issue, InstallmentRecord installmentRecord, BatteryMemberCard memberCard) {
        Integer rentTime = null;
        if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
            if (issue < installmentRecord.getInstallmentNo()) {
                // 非最后一期租期
                LocalDate currentDate = LocalDate.now();
                LocalDate nextMonth = currentDate.plusMonths(issue - 1);
                rentTime = nextMonth.lengthOfMonth();
            } else {
                // 最后一期
                rentTime = memberCard.getValidDays();
                for (int i = 0; i < issue - 1; i++) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate nextMonth = currentDate.plusMonths(i);
                    // 非最后一期的代扣计划的租期
                    int otherSuborderRentTime = nextMonth.lengthOfMonth();
                    rentTime -= otherSuborderRentTime;
                }
            }
        }
        return rentTime;
    }
    
    /**
     * 计算还款计划还款时间，用于在签约完成后生成还款计划
     */
    public static Long calculateSuborderDeductTime(Integer issue) {
        LocalTime currentTime = LocalTime.now();
        LocalDate startDate = LocalDate.now();
        LocalDate currentMonth = startDate.plusMonths(issue - 1);
        
        // 使用 TemporalAdjusters 的 lastDayOfMonth() 方法来处理月份天数不足的情况
        LocalDate nextRepaymentDate = currentMonth.with(TemporalAdjusters.lastDayOfMonth());
        if (startDate.getDayOfMonth() <= nextRepaymentDate.lengthOfMonth()) {
            // 如果当前天数在下个月存在，则直接设置为当前天数
            nextRepaymentDate = currentMonth.withDayOfMonth(startDate.getDayOfMonth());
        }
        
        // 结合时间
        LocalDateTime repaymentDateTime = LocalDateTime.of(nextRepaymentDate, currentTime);
        return repaymentDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
    }
    
    /**
     * 根据用户uid生成基础的代扣计划payNo
     *
     * @param uid 用户uid
     */
    public static void generatePayNo(Long uid, List<InstallmentDeductionPlan> deductionPlans) {
        String uidStr = String.format("%08d", uid);
        String payNo;
        if (uidStr.length() == 8) {
            payNo = uidStr + String.valueOf(System.currentTimeMillis()).substring(0, 10);
        } else {
            payNo = uidStr.substring(0, 8) + String.valueOf(System.currentTimeMillis()).substring(0, 10);
        }
        
        // payNo要求20位字符串
        // 采用规则：uid不够8位，以0凑足8位，超过的取前8位，拼上时间戳的前10位，代扣计划序号0、1、2...组成的2位字符串
        // 在uid未超过1亿时可以完全避免重复
        for (int i = 1; i <= deductionPlans.size(); i++) {
            deductionPlans.get(i).setPayNo(payNo + String.format("%02d", i));
        }
    }
}
