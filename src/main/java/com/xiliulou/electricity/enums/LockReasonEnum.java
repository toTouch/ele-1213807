package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author maxiaodong
 * @date 2024/10/30 21:31
 * @desc
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum LockReasonEnum {

    /**
     * 系统锁仓(0)
     */

    HIGH_TEMPERATURE_PROTECT(23003, "充电高温保护", "电池健康状态检测打开"),
    CHARGE_MOS_FAILURE(23007, "充电MOS损坏", "电池健康状态检测打开"),
    DISCHARGE_MOS_FAILURE(23008, "放电MOS损坏", "电池健康状态检测打开"),
    BATTERY_LOSS(1004, "电池异常消失", null),
    BATTERY_CODE_CHANGE(1007, "电池编码异常改变", null),
    BUSINESS_LOCK(1008, "系统锁仓(业务锁仓)", "租换退业务锁仓"),
    BATTERY_LOSS_DISCHARGE(1012, "电池异常消失-断电", "电池长时间消失"),
    BATTERY_CODE_CHANGE_DISCHARGE(1011, "电池编码异常改变-断电", "电池长时间消失"),
    BATTERY_LOSS_CHARGE(1013, "电池异常出现-断电", "柜机断电锁仓-打开"),


    /**
     * 人工锁仓(1)
     */
    OPEN_FAIL(50001, "开门失败", "远程锁仓"),
    CHARGE_FAIL(50002, "充电器问题", "远程锁仓"),
    HARDWARE_FAILURE(50003, "硬件通讯故障", "远程锁仓"),
    HEATER_FAILURE(50004, "加热故障", "远程锁仓"),
    LIGHT_FAILURE(50005, "指示灯故障", "远程锁仓"),
    OTHERS(50006, "其他", "远程锁仓"),
    SCREEN_LOCK(1002, "屏幕锁仓", "柜机锁仓"),
    ;

    private final Integer code;

    private final String desc;

    private final String remark;
}
