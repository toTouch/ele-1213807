package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-11-11:01
 */
@Data
public class BatteryModelVO {
    /**
     * 电池型号
     */
    private Integer batteryModel;
    /**
     * 电池型号
     */
    private String batteryType;
    /**
     * 电池电压
     */
    private Double batteryV;
    /**
     * 电池短型号
     */
    private String batteryVShort;
}
