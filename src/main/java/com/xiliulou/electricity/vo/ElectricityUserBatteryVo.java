package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 换电柜电池表(ElectricityBattery)实体类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Data
public class ElectricityUserBatteryVo {
    /**
     * sn码
     */
    private String sn;
    /**
     * 电池型号
     */
    private String model;
    /**
     * 电池电量
     */
    private Double power;

    /**
     * 电池的标称电压
     */
    private Double batteryV;
    /**
     * 电池的电流
     */
    private Double batteryA;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;

    private Long updateTime;
    
    /**
     * 换电柜ID
     */
    private Integer electricityCabinetId;
    /**
     * 换电柜名称
     */
    private String electricityCabinetName;
    
    /**
     * 换电时间
     */
    private Long batteryExchangeTime;


}
