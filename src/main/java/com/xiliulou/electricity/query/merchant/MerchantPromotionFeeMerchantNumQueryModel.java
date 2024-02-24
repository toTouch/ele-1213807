package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantPromotionFeeMerchantNumQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionFeeMerchantNumQueryModel {
    
    private Integer tenantId;
    
    private Long uid;
    
    private Long startTime;
    
    private Long endTime;
}
