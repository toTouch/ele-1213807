package com.xiliulou.electricity.query.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BatteryModelQueryModel {
    private Long size;
    private Long offset;
    
    //租户id
    private Integer tenantId;
}
