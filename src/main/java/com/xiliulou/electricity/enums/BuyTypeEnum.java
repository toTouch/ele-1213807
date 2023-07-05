package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 购买方式枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum BuyTypeEnum implements BasicEnum<Integer, String> {

    ON_LINE(1, "线上"),
    OFF_LINE(2, "线下"),
    give(3, "赠送"),
    ;

    private final Integer code;

    private final String desc;
}
