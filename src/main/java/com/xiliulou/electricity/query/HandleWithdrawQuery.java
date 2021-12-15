package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: Miss.Li
 * @Date: 2021/9/2 15:24
 * @Description:
 */
@Data
public class HandleWithdrawQuery {

	@NotNull(message = "id不能为空")
	private Integer id;

	@NotNull(message = "状态不能为空")
	private Integer status;

	private String msg;

	@NotEmpty(message = "密码不能为空")
	private String password;
}
