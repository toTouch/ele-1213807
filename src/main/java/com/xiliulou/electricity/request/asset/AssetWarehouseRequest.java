package com.xiliulou.electricity.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 查询库房
 * @date 2023/11/21 20:15:14
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetWarehouseRequest {
    
    /**
     * 库房名称
     */
    private String name;
    
    private Long size;
    
    private Long offset;
    
    
    
    private Long franchiseeId;
 
}
