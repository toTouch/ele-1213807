package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 19:39
 * @Description:
 */
@Data
public class UserCouponDTO {

    /**
     * 优惠券ID
      */
    private Long couponId;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 来源订单编码
     */
    private String sourceOrderNo;

    /**
     * 链路ID
     */
    private String traceId;
    
    /**
     * 套餐id
     */
    private Long packageId;
    
    /**
     * 发券方式区分类型,1是电，2为车
     */
    private Integer couponWayDiffType;
}
