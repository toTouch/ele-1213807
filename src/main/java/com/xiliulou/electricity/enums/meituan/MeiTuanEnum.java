package com.xiliulou.electricity.enums.meituan;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MeiTuanEnum {
    CHECK_SIGN_ERROR(000, "验签失败");
    
    private final Integer code;
    
    private final String desc;
}
