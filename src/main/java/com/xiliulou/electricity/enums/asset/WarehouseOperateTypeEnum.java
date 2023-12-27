package com.xiliulou.electricity.enums.asset;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author HeYafeng
 * @description 库房资产记录 操作类型
 * @date 2023/12/20 09:45:18
 */

@Getter
@AllArgsConstructor
public enum WarehouseOperateTypeEnum implements BasicEnum<Integer, String> {
    
    WAREHOUSE_OPERATE_TYPE_IN(1, "入库"),
    
    WAREHOUSE_OPERATE_TYPE_OUT(2, "出库"),
    
    WAREHOUSE_OPERATE_TYPE_BATCH_IN(3, "批量入库"),
    
    WAREHOUSE_OPERATE_TYPE_BATCH_OUT(4, "批量出库"),
    
    WAREHOUSE_OPERATE_TYPE_EXIT(5, "退库");
    
    private final Integer code;
    
    private final String desc;
}
