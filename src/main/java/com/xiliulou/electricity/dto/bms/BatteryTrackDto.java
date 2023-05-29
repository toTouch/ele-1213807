package com.xiliulou.electricity.dto.bms;


import lombok.Data;

@Data
public class BatteryTrackDto {

    /**
     * 电池编号
     */
    private String sn;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;

    private String createTime;
}
