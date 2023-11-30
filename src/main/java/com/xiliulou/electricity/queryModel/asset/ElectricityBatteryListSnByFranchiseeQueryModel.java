package com.xiliulou.electricity.queryModel.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 查询电池sn
 * @date 2023/11/24 14:54:54
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityBatteryListSnByFranchiseeQueryModel {
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Integer stockStatus;
    
    private String sn;
    
    private Long size;
    
    private Long offset;
}
