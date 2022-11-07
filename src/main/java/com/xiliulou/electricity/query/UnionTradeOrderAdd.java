package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: Miss.Li
 * @Date: 2022/11/07 09:07
 * @Description:
 */
@Data
public class UnionTradeOrderAdd {

	//保险
	@NotNull(message = "保险不能为空!", groups = {CreateGroup.class})
	private Integer insuranceId;

	//保险
	@NotNull(message = "保险不能为空!", groups = {CreateGroup.class})
	private Long franchiseeId;

	private Integer model;


}
