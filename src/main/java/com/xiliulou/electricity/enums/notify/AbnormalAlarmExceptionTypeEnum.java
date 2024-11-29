/**
 * Create date: 2024/6/26
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
    BATTERY_FULL_TYPE(1, "检测到柜机内电池满仓，暂无法提供换电服务", "柜机电池满仓", "电池满仓"),
    ;
    
    private static final Map<Integer, AbnormalAlarmExceptionTypeEnum> map = new HashMap<>();
    
    static {
        for (AbnormalAlarmExceptionTypeEnum value : values()) {
            map.put(value.type, value);
        }
    }
    
    private Integer type;
    
    private String description;
    
    private String firstName;
    
    private String exceptionName;
    
    AbnormalAlarmExceptionTypeEnum(Integer type, String description, String firstName, String exceptionName) {
        this.type = type;
        this.description = description;
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
