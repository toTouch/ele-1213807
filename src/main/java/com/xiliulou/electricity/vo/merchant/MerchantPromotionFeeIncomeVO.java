package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPromotionFeeIncomeVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Data
public class MerchantPromotionFeeIncomeVO {
    /**
     * 可提现金额
     */
    private BigDecimal availableWithdrawAmount;
    
    /**
     * 今日预估收入
     */
    private BigDecimal todayIncome;
    
    /**
     * 昨日收入
     */
    private BigDecimal yesterdayIncome;
    
    /**
     * 本月预估收入
     */
    private BigDecimal currentMonthIncome;
    
    /**
     * 上月收入
     */
    private BigDecimal lastMonthIncome;
    
    /**
     * 累计收入
     */
    private BigDecimal totalIncome;
    
    /**
     * 今日新增商戶數
     */
    private Integer todayMerchantNum;
    
    /**
     * 本月新增商户数
     */
    private Integer currentMonthMerchantNum;
    
    /**
     * 累计商户数
     */
    private Integer totalMerchantNum;
}
