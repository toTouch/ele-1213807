package com.xiliulou.electricity.entity.asset;

/**
 * @author HeYafeng
 * @description 资产调拨表
 * @date 2023/11/29 11:29:40
 */

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import lombok.Data;

/**
 * 资产调拨记录表
 */
@Data
public class AssetAllocateRecord {
    
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
     * 调拨资产类型 (1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    private Integer assetType;
    
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

