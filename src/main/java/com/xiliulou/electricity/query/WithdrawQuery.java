package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawQuery {
	/**
	 * 提现金额,单位元
	 */
	@NotNull(message = "提现金额不能为空")
	private Double amount;
	/**
	 * 银行名称
	 */
	@NotEmpty(message = "银行名称不能为空")
	private String bankName;
	/**
	 * 银行卡号
	 */
	@NotEmpty(message = "银行卡号不能为空")
	private String bankNumber;

	/**
	 * 用户Id
	 */
	private Long uid;



}
