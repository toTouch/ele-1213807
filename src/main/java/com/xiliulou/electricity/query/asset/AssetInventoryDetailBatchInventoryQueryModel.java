package com.xiliulou.electricity.query.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 批量盘点model
 * @date 2023/11/28 11:37:05
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetInventoryDetailBatchInventoryQueryModel {
    
    /**
     * 盘点单号
     */
    private String orderNo;
    
    /**
     * '盘点状态(0-正常,1-故障, 2-库存, 3-丢失, 4-报废)',
     */
    private Integer status;
    
    /**
     * 盘点的电池sn码
     */
    private List<String> snList;
    
    /**
     * 操作人
     */
    private Long operator;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 租户id
     */
    private Integer tenantId;
}
