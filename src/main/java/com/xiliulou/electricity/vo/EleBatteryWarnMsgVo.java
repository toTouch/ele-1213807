package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: lxc
 * @Date: 2021/6/2 11:23
 * @Description:
 */
@Data
public class EleBatteryWarnMsgVo {

	private String sessionId;

	private String createTime;

	private Long errorCode;

	private String batteryName;

	private String errorMsg;

	private String reportTime;

	private String electricityCabinetId;

	private String cabinetName;

	private Integer tenantId;
}
