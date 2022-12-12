package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: Miss.Li
 * @Date: 2022/12/12 09:07
 * @Description:
 */
@Data
public class IntegratedPaymentAdd {


	private Integer insuranceId;

	private Long franchiseeId;

	private Integer model;

	private Integer memberCardId;

	private Integer userCouponId;


}
