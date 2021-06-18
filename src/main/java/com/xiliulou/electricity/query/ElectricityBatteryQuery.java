package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

@Data
public class ElectricityBatteryQuery {


    /**
     * sn码
     */
    private String sn;

    /**
     * 0：在仓，1：在库，2：租借
     */
    private Integer status;

    private Long uid;

    private List<Long> electricityBatteryIdList;

    private Integer tenantId;


}
