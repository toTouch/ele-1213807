package com.xiliulou.electricity.query.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产盘点详情model
 * @date 2023/11/20 16:40:54
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetInventoryDetailQueryModel {
    
    /**
     * 盘点订单号
     */
    private String orderNo;
    
    /**
     * 是否已盘点(0-未盘点,1-已盘点)
     */
    private Integer inventoryStatus;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    
    private Long size;
    
    private Long offset;
    
}
