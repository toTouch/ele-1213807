package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author renhang
 */

@Getter
@AllArgsConstructor
public enum FreeBusinessTypeEnum {
    
    
    /**
     * 换电类型
     */
    FREE(1, "免押回调"),
    UNFREE(2, "解冻回调"),
    AUTH_PAY(3, "代扣回调"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
}
