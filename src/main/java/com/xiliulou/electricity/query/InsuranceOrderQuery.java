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
public class InsuranceOrderQuery {
    private Long size;
    private Long offset;


    private Integer tenantId;


    private Long franchiseeId;

    private String franchiseeName;

    private String userName;

    private String phone;

    private String orderId;

    private Long beginTime;

    private Long endTime;

    private Integer status;

    private Integer isUse;

    private Long uid;

    private Integer payType;
}
