package com.xiliulou.electricity.enums.asset;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author HeYafeng
 * @description 资产盘点详情信息表中的 资产盘点状态
 * @date 2023/11/20 11:13:18
 */

@Getter
@AllArgsConstructor
public enum AssetInventoryDetailStatusEnum {
    ASSET_INVENTORY_DETAIL_STATUS_NORMAL(0, "正常"),
    ASSET_INVENTORY_DETAIL_STATUS_FAILURE(1, "故障"),
    ASSET_INVENTORY_DETAIL_STATUS_INVENTORY(2, "库存"),
    ASSET_INVENTORY_DETAIL_STATUS_LOST(3, "丢失"),
    ASSET_INVENTORY_DETAIL_STATUS_SCRAP(4, "报废");
    
    private final Integer code;
    
    private final String desc;
    
}
