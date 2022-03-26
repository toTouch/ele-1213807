package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hrp
 * @date 2022/3/26 13:37
 * @mood 数据大屏优惠券统计
 */
@Data
public class CouponStatisticVo {

    /**
     * 优惠券发放数量
     */
    private Integer couponIssueCount;

    /**
     * 优惠券使用数量
     */
    private Integer couponUseCount;

    /**
     * 周优惠券发放数量
     */
    private List<WeekCouponStatisticVo> weekCouponIssue;

    /**
     * 周优惠券使用数量
     */
    private List<WeekCouponStatisticVo> weekCouponUse;

}
