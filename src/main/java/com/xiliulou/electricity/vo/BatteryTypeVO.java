package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-15-11:14
 */
@Data
public class BatteryTypeVO {

    /**
     * 电池型号
     */
    private Integer batteryModel;
    /**
     * 电池型号
     */
    private String batteryType;

    private String batteryTypeName;
    /**
     * 电池电压
     */
    private Double batteryV;
    /**
     * 电池短型号
     */
    private String batteryVShort;
}
