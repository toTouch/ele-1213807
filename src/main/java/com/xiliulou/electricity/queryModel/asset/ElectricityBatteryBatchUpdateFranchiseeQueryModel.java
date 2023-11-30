package com.xiliulou.electricity.queryModel.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产调拨：电池调拨model
 * @date 2023/11/29 20:17:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityBatteryBatchUpdateFranchiseeQueryModel {
    
    /**
     * 电池id
     */
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 调出加盟商id
     */
    private Long sourceFranchiseeId;
    
    /**
     * 调入加盟商id
     */
    private Long targetFranchiseeId;
    
    private String sn;
    
}
