package com.xiliulou.electricity.vo.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 库房资产记录VO
 * @date 2023/12/20 10:29:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetWarehouseRecordVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 记录单号
     */
    private String recordNo;
    
    /**
     * 资产类型(1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 操作类型(1-入库, 2-出库, 3-批量入库, 4-批量出库, 5-退库)
     *
     * @see WarehouseOperateTypeEnum
     */
    private Integer operateType;
    
    /**
     * 资产SN码
     */
    private List<String> snList;
    
    /**
     * 库房ID
     */
    private Long warehouseId;
    
    /**
     * 库房名称
     */
    private String warehouseName;
    
    /**
     * 操作人
     */
    private Long operator;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
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
