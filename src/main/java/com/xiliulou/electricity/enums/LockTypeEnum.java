package com.xiliulou.electricity.enums;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 锁仓类型
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum LockTypeEnum {

    SYSTEM_LOCK(0, "系统锁仓"),
    ARTIFICIAL_LOCK(1, "人为锁仓"),
    ;

    private final Integer code;

    private final String desc;


    public static Boolean lockTypeCodeByDefined(Integer code) {
        return CollUtil.newArrayList(SYSTEM_LOCK.getCode(), ARTIFICIAL_LOCK.getCode()).contains(code);
    }
}
