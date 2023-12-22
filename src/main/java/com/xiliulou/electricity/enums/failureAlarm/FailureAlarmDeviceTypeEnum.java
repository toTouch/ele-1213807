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
public enum FailureAlarmDeviceTypeEnum implements BasicEnum<Integer, String> {
   
    FAILURE_ALARM_DEVICE_TYPE_BATTERY(1, "电池"),
    
    FAILURE_ALARM_DEVICE_TYPE_CABINET(2, "换电柜");
    
    
    private final Integer code;
    
    private final String desc;
}
