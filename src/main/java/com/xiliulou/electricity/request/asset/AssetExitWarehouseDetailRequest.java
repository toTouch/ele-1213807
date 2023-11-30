package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 退库详情
 * @date 2023/11/27 19:30:31
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetExitWarehouseDetailRequest {
    
    /**
     * 盘点订单号
     */
    private String orderNo;
    
    private Long size;
    
    private Long offset;
    
}
