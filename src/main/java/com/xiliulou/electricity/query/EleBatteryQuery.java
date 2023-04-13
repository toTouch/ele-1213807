package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author : eclair
 * @date : 2023/4/12 17:34
 */
@Data
public class EleBatteryQuery {

    private Integer id;
    /**
     * sn码
     */
    @NotEmpty(message = "电池编码不能不能为空!")
    private String sn;
    /**
     * 电池型号
     */
    private String model;
    /**
     * 电压
     */
    private Integer voltage;
    /**
     * 电池容量,单位(mah)
     */
    private Integer capacity;

    /**
     * 物联网卡号
     */
    private String iotCardNumber;

    private Boolean isNeedSync;


}
