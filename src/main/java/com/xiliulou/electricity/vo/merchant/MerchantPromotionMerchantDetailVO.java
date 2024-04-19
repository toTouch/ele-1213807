package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantEmployeeDetailVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Data
public class MerchantPromotionMerchantDetailVO {
    private Long uid;
    
    private String merchantName;
    
    private BigDecimal todayIncome;
    
    private BigDecimal currentMonthIncome;
    
    private BigDecimal totalIncome;
}
