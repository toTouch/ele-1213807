package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: lxc
 * @Date: 2021/6/2 11:23
 * @Description:
 */
@Data
public class EleBusinessWarnMsgVo {

	private String createTime;

	private Long errorCode;

	private String errorMsg;

	private Integer cellNo;

	private String reportTime;

	private String electricityCabinetId;

	private String cabinetName;

	private Integer tenantId;
}
