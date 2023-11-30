package com.xiliulou.electricity.bo.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 资产退库记录BO
 * @date 2023/11/28 09:10:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetExitWarehouseBO {
    
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
     * 退库仓库名称
     */
    private Long warehouseName;
    
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
    
}
