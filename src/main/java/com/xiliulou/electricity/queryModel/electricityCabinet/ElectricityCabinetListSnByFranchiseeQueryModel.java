package com.xiliulou.electricity.queryModel.electricityCabinet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 查询柜机sn
 * @date 2023/11/24 14:54:54
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ElectricityCabinetListSnByFranchiseeQueryModel {
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    /**
     * 库存状态：0,库存；1,已出库
     */
    private Integer stockStatus;
    
    private Long size;
    
    private Long offset;
}
