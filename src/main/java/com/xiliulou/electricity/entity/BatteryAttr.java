package com.xiliulou.electricity.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

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
	private BigDecimal longitude;
	/**
	 * 	纬度 01103001
	 */
	private BigDecimal latitude;
	/**
	 * 	gsm信号强度 01106001
	 */
	private Long gsmSignalStrength;
	/**
	 * 	电池状态 01108001
	 */
	private Long batteryStatus;
	/**
	 * 	速度信息 01109001
	 */
	private Long speed;
	/**
	 * 	行驶里程 01110001
	 */
	private Long tripMiles;
	/**
	 * 	总电压 01111001
	 */
	private Long sumV;
	/**
	 * 	总电流 01112001
	 */
	private Long sumA;
	/**
	 * 	soc 电池电量 01113001
	 */
	private Long soc;
	/**
	 * 	剩余容量  01115001
	 */
	private Long remainCapacity;
	/**
	 * 	功率温度值  01118001
	 */
	private Long powerTemp;
	/**
	 * 	电芯温度值  01119001
	 */
	private Long batteryCoreTemp;
	/**
	 * 	环境温度  01120001
	 */
	private Long envTemp;
	/**
	 * 	总放电 01124001
	 */
	private Long sumDischarge;
	/**
	 * 	总充电 01125001
	 */
	private Long sumCharge;
	/**
	 * 	预计放电时间 01126001
	 */
	private Long expDischargeTime;
	/**
	 * 	上报时间
	 */
	private Long createTime;
}
