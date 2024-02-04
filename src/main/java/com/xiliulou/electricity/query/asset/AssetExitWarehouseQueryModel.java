package com.xiliulou.electricity.query.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 退库查询model
 * @date 2023/11/28 08:58:18
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetExitWarehouseQueryModel {
    
    /**
     * 盘点订单号
     */
    private String orderNo;
    
    /**
     * 盘点加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 资产类型(1-电柜, 2-电池, 3-车辆)
     */
    private Integer type;
    
    private Long size;
    
    private Long offset;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
}
