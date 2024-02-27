package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : PromotionFeeStatisticAnalysisIncomeVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-22
 */
@Data
public class PromotionFeeStatisticAnalysisIncomeVO {
    private BigDecimal totalIncome;
    
    private String statisticIncomeTime;
}
