package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 锁仓类型
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum LockTypeEnum {

    ARTIFICIAL_LOCK(0, "人为锁仓"),
    SYSTEM_LOCK(1, "系统锁仓"),
    ;

    private final Integer code;

    private final String desc;


    public static Boolean lockTypeCodeByDefined(Integer code) {
        return Arrays.stream(LockTypeEnum.values()).filter(item -> Objects.equals(code, ARTIFICIAL_LOCK.getCode()) || Objects.equals(code, SYSTEM_LOCK.getCode())).collect(Collectors.toList()).contains(code);
    }
}
