package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易方式枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum PayTypeEnum implements BasicEnum<Integer, String> {

    ON_LINE(1, "线上"),
    OFF_LINE(2, "线下"),
    EXEMPT(3, "免押"),
    GIVE(4, "赠送"),
    ;

    private final Integer code;

    private final String desc;
}
