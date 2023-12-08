package com.xiliulou.electricity.queryModel.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 新增和更新资产盘点model
 * @date 2023/11/22 09:39:03
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInventorySaveOrUpdateQueryModel {
    
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
