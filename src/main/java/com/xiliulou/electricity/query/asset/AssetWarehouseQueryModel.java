package com.xiliulou.electricity.query.asset;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房model
 * @date 2023/11/21 20:23:41
 */
@Data
public class AssetWarehouseQueryModel {
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 库房名称
     */
    private String name;
    
    private Long size;
    
    private Long offset;
    
    private List<Long> franchiseeIds;
}
