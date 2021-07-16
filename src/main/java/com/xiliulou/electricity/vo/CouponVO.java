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

	/**
	 * 是否领取 1--已领取  2--未领取
	 */
	private Integer isGet;

	public static final Integer IS_GET = 1;
	public static final Integer IS_NOT_GET = 2;
}
