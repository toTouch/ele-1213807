package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: lxc
 * @Date: 2021/6/2 11:23
 * @Description:
 */
@Data
public class EleCellWarnMsgVo {

	private String sessionId;

	private Long createTime;

	private Long errorCode;

	private Integer cellNo;

	private String errorMsg;

	private Integer operateType;

	private Long reportTime;

	private String electricityCabinetId;

	private String cabinetName;
}
