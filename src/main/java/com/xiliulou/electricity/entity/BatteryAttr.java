package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author: Miss.Li
 * @Date: 2021/10/29 13:58
 * @Description:
 */
@Data
public class BatteryAttr {
	/**
	 * 	电池sn
	 */
	private String devId;
	/**
	 * 	gsm小区信息 01104001
	 */
	private String gsmType;
	/**
	 * 	经度 01102001
	 */
	private Double longitude;
	/**
	 * 	纬度 01103001
	 */
	private Double latitude;
	/**
	 * 	gsm信号强度 01106001
	 */
	private Integer gsmSignalStrength;
	/**
	 * 	电池状态 01108001
	 */
	private Integer batteryStatus;
	/**
	 * 	速度信息 01109001
	 */
	private Integer speed;
	/**
	 * 	行驶里程 01110001
	 */
	private Integer tripMiles;
	/**
	 * 	总电压 01111001
	 */
	private Integer sumV;
	/**
	 * 	总电流 01112001
	 */
	private Integer sumA;
	/**
	 * 	soc 电池电量 01113001
	 */
	private Integer soc;
	/**
	 * 	剩余容量  01115001
	 */
	private Integer remainCapacity;
	/**
	 * 	功率温度值  01118001
	 */
	private Integer powerTemp;
	/**
	 * 	电芯温度值  01119001
	 */
	private Integer batteryCoreTemp;
	/**
	 * 	环境温度  01120001
	 */
	private Integer envTemp;
	/**
	 * 	总放电 01124001
	 */
	private Integer sumDischarge;
	/**
	 * 	总充电 01125001
	 */
	private Integer sumCharge;
	/**
	 * 	预计放电时间 01126001
	 */
	private Integer expDischargeTime;
	/**
	 * 	上报时间
	 */
	private String createTime;
}
