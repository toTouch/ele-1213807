package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 时间单位枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum TimeUnitEnum implements BasicEnum<Integer, String> {

    DAY(1, "天"),
    MINUTE(2, "分钟"),
    ;

    private final Integer code;

    private final String desc;
}
