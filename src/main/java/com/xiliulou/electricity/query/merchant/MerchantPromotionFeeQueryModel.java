package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantPromotionFeeQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionFeeQueryModel {
    private Integer status;
    
    private Integer type;
    
    private Integer tenantId;
    
    private Long uid;
    
    private Long rebateStartTime;
    
    private Long rebateEndTime;
    
    private Long settleStartTime;
    
    private Long settleEndTime;
}
