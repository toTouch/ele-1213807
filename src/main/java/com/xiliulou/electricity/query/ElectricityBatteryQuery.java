package com.xiliulou.electricity.query;

public class ElectricityBatteryQuery {

    private Long shopId;
    /**
     * 代理商id
     */
    private Integer agentId;
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
    private Object status;
    /*
     *所属电柜
     */
    private Integer cabinetId;

}
