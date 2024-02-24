package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPromotionDataVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Data
public class MerchantPromotionDataVO {
    private Integer scanCodeCount;
    
    private Integer purchaseCount;
    
    private Integer renewalCount;
    
    private BigDecimal totalIncome;
}
