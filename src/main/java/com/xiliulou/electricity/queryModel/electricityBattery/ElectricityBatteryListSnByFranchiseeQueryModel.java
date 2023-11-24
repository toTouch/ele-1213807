package com.xiliulou.electricity.queryModel.electricityBattery;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 查询电池sn
 * @date 2023/11/24 14:54:54
 */

@Data
@Builder
public class ElectricityBatteryListSnByFranchiseeQueryModel {
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long size;
    
    private Long offset;
}
