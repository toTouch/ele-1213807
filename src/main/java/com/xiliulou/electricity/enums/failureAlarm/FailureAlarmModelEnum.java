package com.xiliulou.electricity.enums.failureAlarm;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author zhangzhe
 * @date 2023/11/16 13:42
 * @Description:
 **/
@Getter
@AllArgsConstructor
public enum FailureAlarmModelEnum implements BasicEnum<Integer, String> {
   
    FAILURE_ALARM_MODEL_BOARD(1, "主板"),
    
    FAILURE_ALARM_MODEL_SUB_BOARD(2, "子板"),
    
    FAILURE_ALARM_MODEL_BATTERY(3, "电池"),
    
    FAILURE_ALARM_MODEL_BATTERY_EXCEPTION(4, "电池异常消失"),
    FAILURE_ALARM_MODEL_CAR(5, "车辆"),
    FAILURE_ALARM_MODEL_CHARGE(6, "充电器"),
    FAILURE_ALARM_MODEL_BMS(7, " BMS"),;
    
    
    private final Integer code;
    
    private final String desc;
}
