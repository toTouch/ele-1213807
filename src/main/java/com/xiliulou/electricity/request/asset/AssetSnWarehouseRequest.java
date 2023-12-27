package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description
 * @date 2023/12/20 16:33:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetSnWarehouseRequest {
    
    private String sn;
    
    private Long warehouseId;
}
