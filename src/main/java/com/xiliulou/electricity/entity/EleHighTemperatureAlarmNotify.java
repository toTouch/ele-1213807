package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-16-13:50
 */
@Data
public class EleHighTemperatureAlarmNotify {

    /**
     * 柜机名称
     */
    private String cabinetName;
    /**
     * 格挡号
     */
    private Integer cellNo;

    private Double cellHeat;
    /**
     * 异常描述
     */
    private String description;
    /**
     * 发生时间
     */
    private String reportTime;


}
