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

    /**
     * 优惠券使用状态 1--未使用， 2--已使用 ，3--已过期，
     *             4--已核销， 5--使用中， 6--已失效
     */
    private Integer status;

}
