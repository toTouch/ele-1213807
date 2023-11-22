package com.xiliulou.electricity.queue.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 查询资产盘点
 * @date 2023/11/20 13:47:42
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInventoryRequest {
    
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
    
    
    private Long size;
    
    private Long offset;
    
}
