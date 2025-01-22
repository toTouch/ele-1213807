package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum OrderStatusEnum {


    INIT_OPEN_FAIL("INIT_OPEN_FAIL", "旧仓门开门失败", "oldCellOpenFailOrderHandler"),
    INIT_BATTERY_CHECK_FAIL("INIT_BATTERY_CHECK_FAIL", "电池检测失败", "oldCellOpenFailOrderHandler"),
    COMPLETE_OPEN_FAIL("COMPLETE_OPEN_FAIL", "新仓门开门失败", "newCellOpenFailOrderHandler"),

    ;

    private final String code;
    private final String desc;
    private final String service;

    public static String getService(String code) {
        return Arrays.stream(OrderStatusEnum.values()).filter(item -> Objects.equals(code, item.getCode())).map(item -> item.getService()).findFirst().orElse(null);
    }
}
