package com.xiliulou.electricity.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.asset.AssetInventoryDetailStatusEnum;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产盘点详情信息表
 * @date 2023/11/20 11:25:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_asset_inventory_detail")
public class AssetInventoryDetail {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 盘点单号
     */
    private String orderNo;
    
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
     * 是否已盘点(0-未盘点,1-已盘点)
     */
    private Integer inventoryStatus;
    
    /**
     * 盘点状态(0-正常,1-故障, 2-库存, 3-丢失, 4-报废)
     *
     * @see AssetInventoryDetailStatusEnum
     */
    private Integer status;
    
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
    
    
    public static final Integer INVENTORY_STATUS_NO = 0;
    
    public static final Integer INVENTORY_STATUS_YES = 1;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
