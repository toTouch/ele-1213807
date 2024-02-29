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
public class MerchantPromotionEmployeeDetailVO {
    private Long uid;
    
    private String employeeName;
    
    private String placeName;
    
    private BigDecimal todayIncome;
    
    private BigDecimal currentMonthIncome;
    
    private BigDecimal totalIncome;
}
