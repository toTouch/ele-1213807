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
public enum FailureAlarmTenantVisibleEnum implements BasicEnum<Integer, String> {
   
    FAILURE_ALARM_TENANT_VISIBLE_YES(1, "可见"),
    
    FAILURE_ALARM_TENANT_VISIBLE_NO(0, "不可见");
    
    
    private final Integer code;
    
    private final String desc;
}
