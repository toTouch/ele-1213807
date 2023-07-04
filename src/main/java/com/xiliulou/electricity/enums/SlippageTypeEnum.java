package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逾期类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum SlippageTypeEnum implements BasicEnum<Integer, String> {

    EXPIRE(1, "过期"),
    FREEZE(2, "冻结"),
    ;

    private final Integer code;

    private final String desc;
}
