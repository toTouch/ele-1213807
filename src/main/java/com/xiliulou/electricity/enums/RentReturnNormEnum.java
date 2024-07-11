package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName: RentReturnNormEnum
 * @description:
 * @author: renhang
 * @create: 2024-06-11 10:49
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("all")
public enum RentReturnNormEnum {
    
    //--------租电
    ALL_RENT(1, "全部可租电"),
    
    NOT_RENT(2, "不允许租电"),
    
    MIN_RETAIN(3, "最少保留一块电池"),
    
    CUSTOM_RENT(4, "自定义租电"),
    
    //--------退电
    ALL_RETURN(1, "全部可退电"),
    
    NOT_RETURN(2, "不允许退电"),
    
    MIN_RETURN(3, "最少保留一个空仓"),
    
    CUSTOM_RETURN(4, "自定义退电"),
    ;
    
    
    private Integer code;
    
    private String desc;
}
