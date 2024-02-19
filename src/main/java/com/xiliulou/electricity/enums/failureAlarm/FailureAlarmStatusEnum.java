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
public enum FailureAlarmStatusEnum implements BasicEnum<Integer, String> {
   
    FAILURE_ALARM_STATUS_ENABLE(0, "启用"),
    
    FAILURE_ALARM_STATUS_DISABLE(1, "禁用");
    
    
    private final Integer code;
    
    private final String desc;
}
