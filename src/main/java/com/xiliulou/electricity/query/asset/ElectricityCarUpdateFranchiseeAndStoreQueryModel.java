package com.xiliulou.electricity.query.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产调拨：车辆调拨model
 * @date 2023/11/29 20:17:32
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ElectricityCarUpdateFranchiseeAndStoreQueryModel {
    /**
     * 车辆id
     */
    private Long id;
    
    /**
     * 车辆型号Id
     */
    private Integer modelId;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 调出加盟商id
     */
    private Long sourceFranchiseeId;
    
    /**
     * 调出门店id
     */
    private Long sourceStoreId;
    
    /**
     * 调入加盟商id
     */
    private Long targetFranchiseeId;
    
    /**
     * 调入门店id
     */
    private Long targetStoreId;
    
    private String sn;
    
    private Long updateTime;
    
}
