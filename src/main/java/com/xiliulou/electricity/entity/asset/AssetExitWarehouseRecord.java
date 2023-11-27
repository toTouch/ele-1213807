package com.xiliulou.electricity.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产退库记录表
 * @date 2023/11/27 15:18:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_asset_exit_warehouse_record")
public class AssetExitWarehouseRecord {
    
    /**
     * 主键Id
     */
    private Long id;
    
    /**
     * 退库单号
     */
    private String orderNo;
    
    /**
     * 退库加盟商
     */
    private Long franchiseeId;
    
    /**
     * 退库门店
     */
    private Long storeId;
    
    /**
     * 退库类型(1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 退库仓库ID
     */
    private Long warehouseId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
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
     * 操作人
     */
    private Long operator;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
