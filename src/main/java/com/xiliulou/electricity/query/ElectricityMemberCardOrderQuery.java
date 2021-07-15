package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: Miss.Li
 * @Date: 2021/7/15 09:07
 * @Description:
 */
@Data
public class ElectricityMemberCardOrderQuery {

	//月卡
	@NotNull(message = "月卡不能为空!", groups = {UpdateGroup.class})
	private Integer memberId;

	//三元组
	@NotEmpty(message = "三元组不能为空!", groups = {UpdateGroup.class})
	private String productKey;

	//三元组
	@NotEmpty(message = "三元组不能为空!", groups = {UpdateGroup.class})
	private String deviceName;

	//优惠券
	private Integer userCouponId;
}
