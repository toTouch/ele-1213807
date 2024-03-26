package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : MerchantEmployeeDetailQueryModel
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionEmployeeDetailQueryModel {
    private Long size;
    
    private Long offset;
    
    private Integer tenantId;
    
    private Long uid;
    
    private Integer type;
}
