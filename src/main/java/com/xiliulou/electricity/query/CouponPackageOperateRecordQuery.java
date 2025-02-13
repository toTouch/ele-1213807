package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

/**
 * @author : renhang
 * @description CouponPackageOperateRecordQuery
 * @date : 2025-02-05 10:13
 **/
@Data
@Builder
public class CouponPackageOperateRecordQuery {

    private Long size;

    private Long offset;

    private Integer packageId;

    private String name;

    private String phone;

    private Long beginTime;

    private Long endTime;

    private Integer tenantId;
}
