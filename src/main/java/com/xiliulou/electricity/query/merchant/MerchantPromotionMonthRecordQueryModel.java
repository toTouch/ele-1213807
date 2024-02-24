package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 商户推广费月度统计查询
 * @date 2024/2/24 11:15:24
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionMonthRecordQueryModel {
    
    private long size;
    
    private long offset;
    
    private String date;
    
    private Integer tenantId;
}
