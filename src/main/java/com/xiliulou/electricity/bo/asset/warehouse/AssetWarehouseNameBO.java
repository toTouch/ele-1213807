package com.xiliulou.electricity.bo.asset.warehouse;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 库房名称BO
 * @date 2023/11/21 20:47:12
 */

@Data
public class AssetWarehouseNameBO {
    
    /**
     * 库房id
     */
    private Long id;
    
    /**
     * 库房名称
     */
    private String name;
}
