package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum YesNoEnum implements BasicEnum<Integer, String> {

    YES(0, "是"),
    NO(1, "否"),
    ;

    private final Integer code;

    private final String desc;
}
