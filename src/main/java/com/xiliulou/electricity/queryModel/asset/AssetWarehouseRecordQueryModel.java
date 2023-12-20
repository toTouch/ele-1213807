package com.xiliulou.electricity.queryModel.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 库房资产记录分页
 * @date 2023/12/20 10:23:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetWarehouseRecordQueryModel {
    
    /**
     * 库房ID
     */
    private Long warehouseId;
    
    /**
     * 资产类型
     *
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 资产编号
     */
    private String sn;
    
    private Long size;
    
    private Long offset;
    
}
