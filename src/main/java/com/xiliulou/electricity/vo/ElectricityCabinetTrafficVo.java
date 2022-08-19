package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ElectricityCabinetTrafficVo {
    private Long id;
    private Long electricityCabinetId;
    private String electricityCabinetName;
    private BigDecimal sameDayTraffic;
    private BigDecimal sumTraffic;
    private String date;
    private Long createTime;
    private Long updateTime;
}

