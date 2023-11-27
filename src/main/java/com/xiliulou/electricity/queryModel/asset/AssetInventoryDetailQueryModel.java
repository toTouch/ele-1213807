package com.xiliulou.electricity.queryModel.asset;

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
     * 盘点状态
     */
    private Integer status;
    
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
