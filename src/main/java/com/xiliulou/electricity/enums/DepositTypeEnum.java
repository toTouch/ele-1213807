package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 押金类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum DepositTypeEnum implements BasicEnum<Integer, String> {
    NORMAL(1, "正常缴纳"),
    CARRY_FORWARD(2, "转押"),
    ;

    private final Integer code;

    private final String desc;
}
