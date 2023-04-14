package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-13-10:43
 */
@Data
public class BatteryModelPageVO {
    /**
     * id
     */
    private Long id;

    private String material;
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

    /**
     * 1--系统 1--自定义
     */
    private Integer type;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
}
