/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.enums.notify;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * description: 故障上报异常类型
 *
 * @author caobotao.cbt
 * @date 2024/6/26 19:19
 */
@Getter
public enum AbnormalAlarmExceptionTypeEnum {
    BATTERY_FULL_TYPE(1, "柜机电池满仓", "电池满仓"),
    SMOKE_WARN_TYPE(2, "烟雾告警/后门锁异常打开", "烟雾告警"),
    BACK_DOOR_OPEN_TYPE(3, "烟雾告警/后门锁异常打开", "后门异常打开"),
    ;
    
    private static final Map<Integer, AbnormalAlarmExceptionTypeEnum> map = new HashMap<>();
    
    static {
        for (AbnormalAlarmExceptionTypeEnum value : values()) {
            map.put(value.type, value);
        }
    }
    
    private Integer type;
    
    private String firstName;
    
    private String exceptionName;
    
    AbnormalAlarmExceptionTypeEnum(Integer type, String firstName, String exceptionName) {
        this.type = type;
        this.firstName = firstName;
        this.exceptionName = exceptionName;
    }
    
    
    /**
     * 根据type获取枚举
     *
     * @param type
     * @author caobotao.cbt
     * @date 2024/6/26 19:24
     */
    public static Optional<AbnormalAlarmExceptionTypeEnum> getByType(Integer type) {
        AbnormalAlarmExceptionTypeEnum abnormalAlarmExceptionTypeEnum = map.get(type);
        return Optional.ofNullable(abnormalAlarmExceptionTypeEnum);
    }
    
    
}
