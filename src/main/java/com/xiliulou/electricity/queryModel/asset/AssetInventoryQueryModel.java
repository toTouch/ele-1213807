package com.xiliulou.electricity.queryModel.asset;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 资产盘点model
 * @date 2023/11/20 16:40:54
 */
@Data
public class AssetInventoryQueryModel {
    
    /**
     * 盘点订单号
     */
    private String orderNo;
    
    /**
     * 盘点加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 盘点状态
     */
    private Integer status;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    private Long size;
    
    private Long offset;
    
}
