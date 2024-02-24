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
public enum FailureAlarmGradeEnum implements BasicEnum<Integer, String> {
    
   
    FAILURE_ALARM_GRADE_FIRST(1, "一级"),
    
    FAILURE_ALARM_GRADE_SECOND(2, "二级"),
    
    FAILURE_ALARM_GRADE_THIRD(3, "三级"),
    
    FAILURE_ALARM_GRADE_FOURTH(4, "四级");
    
    
    private final Integer code;
    
    private final String desc;
}
