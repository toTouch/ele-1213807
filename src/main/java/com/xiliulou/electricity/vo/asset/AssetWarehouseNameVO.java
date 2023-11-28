package com.xiliulou.electricity.vo.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 库房名称VO
 * @date 2023/11/21 20:47:12
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
