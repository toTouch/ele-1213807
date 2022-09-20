package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author: hrp
 * @Date: 2022/08/19 16:02
 * @Description:
 */
@Data
@Builder
public class CouponIssueOperateRecordQuery {
    private Long size;
    private Long offset;

    private String phone;

    private Integer couponId;

    private Long beginTime;
    private Long endTime;

    private Long uid;

    private String name;

    private Integer tenantId;

}
