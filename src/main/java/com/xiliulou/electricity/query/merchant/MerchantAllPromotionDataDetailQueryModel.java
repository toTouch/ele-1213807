package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantPromotionDataQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantAllPromotionDataDetailQueryModel {
    private Long size;
    
    private Long offset;
    
    private Integer tenantId;
    
    private Long merchantId;
    
    private Long startTime;
    
    private Long endTime;
    
    private Integer status;
}
