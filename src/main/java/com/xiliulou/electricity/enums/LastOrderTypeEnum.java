package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum LastOrderTypeEnum {
    
    
    LAST_RENT_ORDER(1, "上次租电订单"),
    LAST_EXCHANGE_ORDER(2, "上次换电订单"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
}
