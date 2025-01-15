package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckFreezeDaysSourceEnum {
    
    BACK(0, "后台校验"),
    
    TINY_APP(1, "小程序校验");
    
    private final Integer code;
    
    private final String desc;
}
