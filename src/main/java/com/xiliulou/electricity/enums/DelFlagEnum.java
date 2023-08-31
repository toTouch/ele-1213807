package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据有效性枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum DelFlagEnum implements BasicEnum<Integer, String> {
    OK(0, "正常"),
    DEL(1, "删除"),
    ;

    private final Integer code;

    private final String desc;
}
