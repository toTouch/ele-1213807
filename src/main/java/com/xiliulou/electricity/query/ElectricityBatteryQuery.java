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
    private Long size;
    private Long offset;
    /**
     * sn码
     */
    private String sn;
    /**
     * 电池物理状态 0：在仓，1：不在仓
     */
    private Integer physicsStatus;
    /**
     * 电池业务状态：1：已录入，2：租借，3：归还，4：异常交换
     */
    private Integer businessStatus;

    private Long uid;

    private List<Long> electricityBatteryIdList;

    private Integer tenantId;

    private Integer chargeStatus;

    private Long franchiseeId;
    
    private List<Long> franchiseeIds;

    private String electricityCabinetName;
    private String franchiseeName;
}
