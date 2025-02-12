package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

/**
 * @author : renhang
 * @description ElectricityCabinetIdByFilterQuery
 * @date : 2025-02-05 16:27
 **/
@Data
@Builder
public class ElectricityCabinetIdByFilterQuery {

    private Integer tenantId;

    private String name;

    private String address;

    private Long areaId;

}
