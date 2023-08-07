package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 17:01
 * @Description:
 */

@Data
public class CouponIssueOperateRecordVO {

    private Integer id;

    private Integer couponId;

    private Long uid;

    private String name;

    private String operateName;

    private String phone;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;

    /**
     * 优惠券使用状态 1--未使用， 2--已使用 ，3--已过期，
     *             4--已核销， 5--使用中， 6--已失效
     */
    private Integer status;


}
