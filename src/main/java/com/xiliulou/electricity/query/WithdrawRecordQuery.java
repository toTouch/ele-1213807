package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Data
@Builder
public class WithdrawRecordQuery {
	private Long size;
	private Long offset;
	/**
	 * uid
	 */
	private Long uid;
	//
	private Long beginTime;
	//
	private Long endTime;

	private List<Integer> status;

	private String orderId;

	private String phone;
}
