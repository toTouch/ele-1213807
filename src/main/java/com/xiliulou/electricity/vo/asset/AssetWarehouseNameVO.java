package com.xiliulou.electricity.vo.asset;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 库房名称VO
 * @date 2023/11/21 20:47:12
 */

@Data
public class AssetWarehouseNameVO {
    
    /**
     * 库房id
     */
    private Long id;
    
    /**
     * 库房名称
     */
    private String name;
}
