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
     * 链路ID
     */
    private String traceId;

}
