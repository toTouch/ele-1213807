package com.xiliulou.electricity.query.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 更新order model
 * @date 2023/11/22 09:39:03
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInventoryUpdateDataQueryModel {
    
    /**
     * 盘点单号
     */
    private String orderNo;
    
    /**
     * 资产类型 (1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 盘点状态(0-进行中,1-完成)
     */
    private Integer status;
    
    /**
     * 操作人
     */
    private Long operator;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 盘点数量
     */
    private Integer inventoryCount;
    
}
