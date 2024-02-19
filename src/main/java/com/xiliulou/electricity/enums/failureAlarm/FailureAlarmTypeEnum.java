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
public enum FailureAlarmTypeEnum implements BasicEnum<Integer, String> {
    
    /**
     * 故障
     */
    FAILURE_ALARM_TYPE_FAILURE(1, "故障"),
    
    /**
     * 告警
     */
    FAILURE_ALARM_TYPE_WARING(2, "告警");
    
    
    private final Integer code;
    
    private final String desc;
}
