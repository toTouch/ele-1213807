package com.xiliulou.electricity.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产调拨详情表
 * @date 2023/11/29 17:39:34
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_asset_allocate_detail")
public class AssetAllocateDetail {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 调拨单号
     */
    private String orderNo;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 资产SN码
     */
    private String assetSn;
    
    /**
     * 资产型号ID
     */
    private Long assetModelId;
    
    /**
     * 调拨资产类型(1-电柜, 2-电池, 3-车辆)
     */
    private Integer assetType;
    
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
    
}
