package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderDataModeEnums {
    
    /**
     * 订单类型
     */
    CURRENT_ORDER(1, "近3个月订单"),
    HISTORY_ORDER(2, "历史订单"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
}
