package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckQuery {
	/**
	 * 姓名
	 */
	@NotEmpty(message = "姓名不能为空")
	private String name;
	/**
	 * 身份证后四位
	 */
	@NotEmpty(message = "身份证后四位不能为空")
	private String idNumber;

	private Long uid;

}
