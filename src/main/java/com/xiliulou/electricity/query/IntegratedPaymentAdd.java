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

	private String productKey;

	private String deviceName;


    /**
     * 保险id
     */
	private Integer insuranceId;

    /**
     * 加盟商id
	 */
	private Long franchiseeId;

    /**
     * 电池类型
	 */
	private Integer model;

    /**
     * 月卡id
	 */
	private Integer memberCardId;

    /**
     * 优惠券id
	 */
	private Integer userCouponId;


}
