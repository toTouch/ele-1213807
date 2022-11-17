package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-16-10:02
 */
@Data
public class BatteryMultiConfigDTO {
    /**
     * 多型号电池长名字
     * */
    private String batteryLongName;
    
    /**
     * 多型号电池短名字
     * */
    private String batteryShortName;
    
    /**
     * 多型号电池默认充电器电压
     * */
    private Float batteryDefaultChargeV;
    
    /**
     * 多型号电池设置的充电器电压
     * 如果该值不为空则优先这个
     * */
    private Float batterySettingChargeV;
    
    /**
     * 多型号电池充电电流
     * */
    private Integer batteryChargeA;
    
    /**
     * 多型号电池最大充电电压
     * MULTI_V
     * */
    private Float batteryMaxChargeV;
    
    /**
     * 多型号电池最低充电电压
     * MULTI_V
     * */
    private Float batteryMinChargeV;
    
    /**
     * 多型号电池可换电电压
     * MULTI_V
     * */
    private Float batteryCriterionV;
}
