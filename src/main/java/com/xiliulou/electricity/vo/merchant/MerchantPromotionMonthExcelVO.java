package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 商户推广费详情
 * @date 2024/2/24 13:02:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantPromotionMonthExcelVO {
    
    private String monthDate;
    
    private String merchantName;
    
    private BigDecimal monthFirstMoney;
    
    private BigDecimal monthRenewMoney;
    
    private String inviterName;
    
    private String typeName;
    
    private BigDecimal dayMoney;
    
    private String date;
}
