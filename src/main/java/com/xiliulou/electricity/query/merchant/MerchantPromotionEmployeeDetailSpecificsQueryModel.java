package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantPromotionEmployeeDetailSpecificsQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionEmployeeDetailSpecificsQueryModel {
    private Long size;
    
    private Long offset;
    
    private Integer tenantId;
    
    private Long uid;
    
    private Integer type;
    
    private Long startTime;
    
    private Long endTime;
    
    private Integer status;
}
