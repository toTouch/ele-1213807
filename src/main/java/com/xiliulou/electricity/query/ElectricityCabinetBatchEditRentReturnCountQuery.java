package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @ClassName: ElectricityCabinetBatchEditRentReturnCountQuery
 * @description:
 * @author: renhang
 * @create: 2024-05-08 09:35
 */
@Data
public class ElectricityCabinetBatchEditRentReturnCountQuery {
    
    /**
     * 柜机id
     */
    @NotNull(message = "id不能为空")
    private Integer id;
    
    /**
     * 最小保留电池数量
     */
    @Min(value = 0, message = "最小保留电池数量不能小于0")
    @Max(value = 99, message = "最小保留电池数量不能超过99")
    private Integer minRetainBatteryCount;
    
    /**
     * 最大保留电池数量
     */
    @Min(value = 0, message = "最小保留电池数量不能小于0")
    @Max(value = 99, message = "最小保留电池数量不能超过99")
    private Integer maxRetainBatteryCount;
    
}
