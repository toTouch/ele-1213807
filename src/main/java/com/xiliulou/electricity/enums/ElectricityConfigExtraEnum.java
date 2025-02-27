package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统设置扩展表相关枚举
 */
@Getter
@AllArgsConstructor
public enum ElectricityConfigExtraEnum {
    SWITCH_ON(0, "开启"),
    SWITCH_OFF(1, "关闭");
    
    private final Integer code;
    
    private final String desc;
}
