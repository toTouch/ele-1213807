package com.xiliulou.electricity.vo.asset;

import com.xiliulou.electricity.enums.asset.AssetInventoryDetailStatusEnum;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 盘点资产详情VO
 * @date 2023/11/20 17:23:58
 */

@Data
public class AssetInventoryDetailVO {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 编号(电池，电柜，车辆)
     */
    private String sn;
    
    /**
     * 资产类型 (1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 所属加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 盘点状态(0-正常,1-故障, 2-库存, 3-丢失, 4-报废)
     *
     * @see AssetInventoryDetailStatusEnum
     */
    private Integer status;
    
    /**
     * 出库时间
     */
    private Long outWarehouseTime;
    
    /**
     * 操作人
     */
    private Long operator;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 备注
     */
    private String remark;
    
}
