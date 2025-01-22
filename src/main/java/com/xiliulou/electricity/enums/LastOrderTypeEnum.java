package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum LastOrderTypeEnum {


    LAST_RENT_ORDER(1, "上次租电订单", "lastRentOrderSuccessHandler"),
    LAST_EXCHANGE_ORDER(2, "上次换电订单", "lastExchangeOrderSuccessHandler"),

    ;

    private final Integer code;

    private final String desc;

    private final String service;

    public static String getService(Integer code) {
        return Arrays.stream(LastOrderTypeEnum.values()).filter(item -> Objects.equals(code, item.getCode())).map(item -> item.getService()).findFirst().orElse(null);
    }
}
