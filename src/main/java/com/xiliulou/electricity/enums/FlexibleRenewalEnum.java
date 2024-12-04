package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 15927
 */

@Getter
@AllArgsConstructor
public enum FlexibleRenewalEnum {
    
    // 灵活续费换电类型，供前端判断怎么弹窗提示以及做什么操作
    NORMAL(0, "正常换电，关闭灵活续费"),
    RETURN_BEFORE_RENT(1, "灵活续费-先还后租"),
    EXCHANGE_BATTERY(2, "灵活续费-换电"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
