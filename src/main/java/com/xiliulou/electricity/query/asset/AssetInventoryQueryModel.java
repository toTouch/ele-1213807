package com.xiliulou.electricity.query.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 资产盘点model
 * @date 2023/11/20 16:40:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
     * 盘点状态(0-进行中,1-完成)
     */
    private Integer status;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    private Long size;
    
    private Long offset;
    
    private List<Long> franchiseeIds;
}
