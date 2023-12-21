package com.xiliulou.electricity.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 库房资产记录详情实体类
 * @date 2023/12/20 09:42:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_asset_warehouse_detail")
public class AssetWarehouseDetail {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 记录单号
     */
    private String recordNo;
    
    /**
     * 库房ID
     */
    private Long warehouseId;
    
    /**
     * 资产类型(1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 资产sn
     */
    private String sn;
    
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
     * 备注
     */
    private String remark;
}
