package com.xiliulou.electricity.entity;
import lombok.Data;

/**
 * 电池型号(ElectricityBatteryModel)实体类
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
@Data
public class BatteryFormat {
    /**
     * 电压
     */
    private Integer voltage;
    /**
     * 电池容量,单位(mah)
     */
    private Integer capacity;

}