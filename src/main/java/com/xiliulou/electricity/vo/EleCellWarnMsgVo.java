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

    private String createTime;

    private Long errorCode;

    private Integer cellNo;

    private String errorMsg;

    private Integer operateType;

    private String reportTime;

    private String electricityCabinetId;

    private String cabinetName;

    private Integer tenantId;
}
