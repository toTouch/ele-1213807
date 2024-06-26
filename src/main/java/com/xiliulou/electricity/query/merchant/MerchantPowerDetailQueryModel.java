package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 场地电费详情
 * @date 2024/2/25 11:04:48
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPowerDetailQueryModel {
    
    private Integer tenantId;
    
    private String monthDate;
    
    private List<Long> franchiseeIds;
    
    private Long franchiseeId;
}
