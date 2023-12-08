package com.xiliulou.electricity.bo.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 库房名称BO
 * @date 2023/11/21 20:47:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
