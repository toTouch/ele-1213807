package com.xiliulou.electricity.enums;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static List<Integer> list = CollUtil.newArrayList();

    @PostConstruct
    public void init() {
        list.add(SYSTEM_LOCK.getCode());
        list.add(ARTIFICIAL_LOCK.getCode());
    }

    public static Boolean lockTypeCodeByDefined(Integer code) {
        return list.contains(code);
    }
}
