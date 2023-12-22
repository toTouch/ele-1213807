package com.xiliulou.electricity.bo.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author HeYafeng
 * @description 库房资产记录BO
 * @date 2023/12/20 09:42:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetWarehouseRecordBO {
    
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
     * @see AssetTypeEnum
     */
    private Integer type;
    
    /**
     * 操作类型(1-入库, 2-出库, 3-批量入库, 4-批量出库, 5-退库)
     * @see WarehouseOperateTypeEnum
     */
    private Integer operateType;
    
    /**
     * 库房ID
     */
    private Long warehouseId;
    
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
    
    /**
     * 库房名称
     */
    private String warehouseName;
    
    // equals 和 hashCode 方法，用于去重
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssetWarehouseRecordBO that = (AssetWarehouseRecordBO) o;
        return Objects.equals(recordNo, that.recordNo) && Objects.equals(warehouseId, that.warehouseId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(recordNo, warehouseId);
    }
}
