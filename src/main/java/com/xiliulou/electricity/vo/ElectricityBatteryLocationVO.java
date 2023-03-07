package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/3/7 10:19
 */
@Data
public class ElectricityBatteryLocationVO {
    private String sn;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;
}
