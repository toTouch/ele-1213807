package com.xiliulou.electricity.dto.battery;

import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author SJP
 * @date 2025-02-17 09:53
 **/
@Data
@AllArgsConstructor
public class ElectricityBatteryLabelDTO {
    
    /**
     * sn码
     */
    private String sn;
    
    /**
     * 电池标签
     * @see BatteryLabelEnum
     */
    private Integer label;
}
