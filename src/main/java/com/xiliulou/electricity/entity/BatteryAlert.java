package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author: Miss.Li
 * @Date: 2021/10/29 14:40
 * @Description:
 */
@Data
public class BatteryAlert {
	/**
	 * 	电池sn
	 */
	private String devId;
	/**
	 * 	dtu故障 01001001
	 */
	private String dtuHealth;
	/**
	 * 	bms故障 01002001
	 */
	private String bmsHealth;
	/**
	 * 	bms告警 01003001
	 */
	private String bmsWarning;
	/**
	 * 	上报时间
	 */
	private Long createTime;
	/**
	 * 	告警时间
	 */
	private Long alarmTime;
	/**
	 * 	告警标识
	 */
	private Integer alarmFlag;
	/**
	 * 	type
	 */
	private String type;

	/**
	 * 	DTU故障
	 */
	public static final String TYPE_DTU_HEALTH = "DTU_HEALTH";

	/**
	 * 	BMS故障
	 */
	public static final String TYPE_BMS_HEALTH = "BMS_HEALTH";

	/**
	 * 	BMS告警
	 */
	public static final String TYPE_BMS_WARNING = "BMS_WARNING";
}
