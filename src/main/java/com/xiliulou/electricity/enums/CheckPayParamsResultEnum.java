package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 校验支付参数结果
 * @author Songjp
 */
@Getter
@AllArgsConstructor
public enum CheckPayParamsResultEnum {
    
    SUCCESS(1, "校验成功"),
    
    FAIL(0, "校验失败");
    
    private final Integer code;
    
    private final String desc;
}
