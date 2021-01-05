package com.xiliulou.electricity.query;

import lombok.Data;

@Data
public class ElectricityBatteryQuery {

    private Integer shopId;
    /**
     * sn码
     */
    private String serialNumber;
    /**
     * 型号id
     */
    private Integer modelId;

    /**
     * 0：在仓，1：在库，2：租借
     */
    private Integer status;
    /*
     *所属电柜
     */
    private Integer cabinetId;

}
