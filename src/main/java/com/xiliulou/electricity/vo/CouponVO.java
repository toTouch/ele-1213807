package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.Coupon;
import lombok.Data;

/**
 * @author: Miss.Li
 * @Date: 2021/7/15 10:03
 * @Description:
 */
@Data
public class CouponVO {

	/**
	 * 天数劵
	 */
	private Integer triggerCount;

	/**
	 * 优惠券
	 */
	private Coupon coupon;
}
