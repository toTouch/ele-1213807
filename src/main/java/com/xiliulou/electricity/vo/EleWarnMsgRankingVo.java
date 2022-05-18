package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 电柜异常信息视图
 *
 * @author HRP
 * @since 2022-03-15 19:132:06
 */
@Data
@Builder
public class EleWarnMsgRankingVo {

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 硬件异常信息数量
     */
    private Integer hardwareWarnMsgCount;

    /**
     * 系统异常信息数量
     */
    private Integer systemWarnMsgCount;

    /**
     * 换电柜名字
     */
    private String electricityCabinetName;


    private Integer electricityCabinetId;

}
