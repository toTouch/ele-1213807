package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
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

    private Integer chargeStatus;

    private Long franchiseeId;

    private String electricityCabinetName;
    private String franchiseeName;
}
