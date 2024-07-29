package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-14-16:41
 */
@Data
public class ElectricityCabinetTransferQuery {
    
    @NotBlank(message = "productKey不能为空!")
    private String productKey;
    
    @NotBlank(message = "deviceName不能为空!")
    private String deviceName;
    
    @NotBlank(message = "name不能为空!")
    private String name;
    
    /**
     * 产品编码 sn
     */
    private String cabinetSn;
    
    @NotNull(message = "门店id不能为空!")
    private Long storeId;
    
    private Double longitude;
    
    private Double latitude;
    
    private String address;
    
    private Integer modelId;
    
    
    /**
     * 租电类型（全部可租电、不允许租电、最少保留一块电池、自定义） RentReturnNormEnum
     */
    @NotNull(message = "租电类型不能为空!")
    private Integer rentTabType;
    
    /**
     * 退电类型（全部可退电、不允许退电、最少保留一个空仓、自定义） RentReturnNormEnum
     */
    @NotNull(message = "退电类型不能为空!")
    private Integer returnTabType;
    
    /**
     * 最小保留电池数量，只有自定义才需要
     */
    @Min(value = 0, message = "最小保留电池数量不能小于0")
    @Max(value = 99, message = "最小保留电池数量不能超过99")
    private Integer minRetainBatteryCount;
    
    /**
     * 最大保留电池数量，只有自定义才需要
     */
    @Min(value = 0, message = "最大保留电池数量不能小于0")
    @Max(value = 99, message = "最大保留电池数量不能超过99")
    private Integer maxRetainBatteryCount;
    
    
    //全天
    public static final String ALL_DAY = "-1";
    
    //自定义时间段
    public static final String CUSTOMIZE_TIME = "1";
    
}
