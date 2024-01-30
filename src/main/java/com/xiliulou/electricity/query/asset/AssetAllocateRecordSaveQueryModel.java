package com.xiliulou.electricity.query.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 新增资产调拨model
 * @date 2023/11/29 17:47:14
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetAllocateRecordSaveQueryModel {
    
    /**
     * 操作Id
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
     * 调拨资产类型 (1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 旧加盟商
     */
    private Long oldFranchiseeId;
    
    /**
     * 旧门店
     */
    private Long oldStoreId;
    
    /**
     * 新加盟商
     */
    private Long newFranchiseeId;
    
    /**
     * 新门店
     */
    private Long newStoreId;
    
    /**
     * 调拨原因
     */
    private String remark;
    
    /**
     * 操作人
     */
    private Long operator;
    
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
