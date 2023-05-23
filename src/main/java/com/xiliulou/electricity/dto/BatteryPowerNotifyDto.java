package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/5/9 19:17
 */
@Data
public class BatteryPowerNotifyDto {
    private String sn;
    private Integer soc;
}
