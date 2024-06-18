package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 商户推广费日结统计查询
 * @date 2024/2/24 11:15:24
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPromotionDayRecordQueryModel {
    
    private Integer tenantId;
    
    private String startDate;
    
    private String endDate;
    
    private List<Long> franchiseeIds;
}
