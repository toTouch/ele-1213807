package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 自定义换电责任链类型枚举
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum ExchangeAssertChainTypeEnum {
    
    QUICK_EXCHANGE_ASSERT(1, "快捷换电"),
    RENT_RETURN_BATTERY_LESS_OPEN_FULL_ASSERT(2, "租电/退电短时间内自主开仓"),
    ;
    
    private Integer code;
    
    private String desc;
}
