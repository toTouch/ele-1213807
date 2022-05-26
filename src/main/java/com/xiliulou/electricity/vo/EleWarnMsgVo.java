package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 电柜异常信息视图
 *
 * @author HRP
 * @since 2022-03-15 19:132:06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EleWarnMsgVo {

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
     * 租户id
     */
    private Integer tenantId;


    /**
     * 换电柜名字
     */
    private String electricityCabinetName;


    private Integer electricityCabinetId;

    /**
     * 仓门号
     */
    private Integer cellNo;


}
