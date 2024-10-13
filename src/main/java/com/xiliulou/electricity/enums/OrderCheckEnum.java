package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum OrderCheckEnum {
    
    CHECK(1, "check接口"),
    ORDER(2, "换电接口");
    
    private final Integer code;
    
    private final String desc;
    
    
}
