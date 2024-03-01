package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 商户推广费详情
 * @date 2024/2/24 13:02:50
 */
@Data
public class MerchantPromotionMonthDetailVO {
    
    private Integer tenantId;
    
    private Long merchantId;
    
    private Long inviterUid;
    
    private BigDecimal monthFirstMoney;
    
    private BigDecimal monthRenewMoney;
    
    private BigDecimal dayFirstMoney;
    
    private BigDecimal dayRenewMoney;
    
    private BigDecimal dayBalanceMoney;
    
    private BigDecimal balanceFromFirst;
    
    private BigDecimal balanceFromRenew;
    
    private Integer type;
    
    
    private String date;
}
