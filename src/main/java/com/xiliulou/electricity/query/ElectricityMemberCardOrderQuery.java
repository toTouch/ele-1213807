package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author: Miss.Li
 * @Date: 2021/7/15 09:07
 * @Description:
 */
@Data
public class ElectricityMemberCardOrderQuery {

	//卡
	private Integer memberId;

	//三元组
	private String productKey;

	//三元组
	private String deviceName;

	//优惠券
	private Integer userCouponId;
}
