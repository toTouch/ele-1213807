package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 柜机异常发送公众号通知
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-10-17-19:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectricityAbnormalMessageNotify {

    /**
     * 网点名称
     */
    private String address;
    /**
     * 设备编号
     */
    private String equipmentNumber;
    /**
     * 异常类型
     */
    private Integer exceptionType;
    /**
     * 异常描述
     */
    private String description;
    /**
     * 发生时间
     */
    private String reportTime;


    //异常类型  1: 电池满仓 2：烟雾告警 ，3:后门异常打开
    public static final Integer BATTERY_FULL_TYPE = 1;
    public static final Integer SMOKE_WARN_TYPE = 2;
    public static final Integer BACK_DOOR_OPEN_TYPE = 3;

    //异常描述
    public static final String BATTERY_FULL_MSG = "检测到柜机内电池满仓，暂无法提供换电服务";
    public static final String SMOKE_WARN_MSG = "系统检测到柜机内出现大量烟雾";
    public static final String BACK_DOOR_OPEN_MSG = "系统检测到柜机后门锁异常打开，疑似暴力开锁";

}
