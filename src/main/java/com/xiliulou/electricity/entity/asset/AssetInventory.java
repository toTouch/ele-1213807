package com.xiliulou.electricity.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.asset.AssetInventoryStatusEnum;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产盘点表
 * @date 2023/11/20 13:07:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_asset_inventory")
public class AssetInventory {
    
    /**
     * 主键Id
     */
    private Long id;
    
    /**
     * 盘点单号
     */
    private String orderNo;
    
    /**
     * 盘点加盟商ID
     */
    private Long franchiseeId;
    
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
     * 已盘点数
     */
    private Integer inventoriedTotal;
    
    /**
     * 待盘点数
     */
    private Integer pendingTotal;
    
    /**
     * 盘点结束时间
     */
    private Long finishTime;
    
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
    
    
    /**
     * 盘点状态：进行中
     */
    public static final Integer ASSET_INVENTORY_STATUS_TAKING=0;
    
    /**
     * 盘点状态：完成
     */
    public static final Integer ASSET_INVENTORY_STATUS_FINISHED=1;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
