package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 查询资产盘点详情
 * @date 2023/11/20 13:47:42
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInventoryDetailRequest {
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 盘点订单号
     */
    private String orderNo;
    
    /**
     * 盘点状态
     */
    private Integer status;
    
    /**
     * 操作人uid
     */
    private Long uid;
    
    
    private Long size;
    
    private Long offset;
    
}
