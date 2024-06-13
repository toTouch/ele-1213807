package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @ClassName: RentReturnEditQuery
 * @description:
 * @author: renhang
 * @create: 2024-06-13 09:55
 */
@Data
public class RentReturnEditQuery {
    
    /**
     * 物联网productKey
     */
    @NotBlank(message = "物联网productKey")
    private String productKey;
    
    /**
     * 物联网deviceName
     */
    @NotBlank(message = "deviceName")
    private String deviceName;
    
    /**
     * 租电类型（全部可租电、不允许租电、最少保留一块电池、自定义） RentReturnNormEnum
     */
    @NotNull(message = "租电类型不能为空")
    private Integer rentTabType;
    
    /**
     * 退电类型（全部可退电、不允许退电、最少保留一个空仓、自定义） RentReturnNormEnum
     */
    @NotNull(message = "退电类型不能为空")
    private Integer returnTabType;
    
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
