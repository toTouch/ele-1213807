package com.xiliulou.electricity.queryModel.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 退库详情model
 * @date 2023/11/28 10:05:51
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetExitWarehouseDetailQueryModel {
    
    /**
     * 盘点订单号
     */
    private String orderNo;
    
    /**
     * 资产类型(1-电柜, 2-电池, 3-车辆)
     */
    private Integer type;
    
    private Long size;
    
    private Long offset;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
}
