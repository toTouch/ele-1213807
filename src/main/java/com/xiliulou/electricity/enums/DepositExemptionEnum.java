package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 押金免押方式枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum DepositExemptionEnum implements BasicEnum<Integer, String> {

    NO(1, "否"),
    SESAME_CREDIT(2, "芝麻信用"),
    ;

    private final Integer code;

    private final String desc;
}
