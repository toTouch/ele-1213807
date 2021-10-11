package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: lxc
 * @Date: 2021/6/2 11:23
 * @Description:
 */
@Data
public class BigEleBatteryVo {
	//电量
	private Double power;
	/**
	 * 仓门号
	 */
	private String cellNo;

	/**
	 * 类型
	 */
	private String batteryType;
}
