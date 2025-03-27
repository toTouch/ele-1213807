package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum PlaceOrderTypeEnum {
    PLACE_ORDER_DEPOSIT(1, "押金缴纳"),
    PLACE_ORDER_MEMBER_CARD(2, "套餐购买"),
    PLACE_ORDER_DEPOSIT_AND_MEMBER_CARD(3, "押金及套餐"),
    PLACE_ORDER_INSURANCE(4, "保险购买"),
    PLACE_ORDER_MEMBER_CARD_AND_INSURANCE(6, "套餐及保险购买"),
    PLACE_ORDER_ALL(7, "押金、套餐及保险购买"),
    FREE_SERVICE_FEE(8, "免押服务费"),
    ;


    public static String getDescByType(Integer type) {
        return Arrays.stream(PlaceOrderTypeEnum.values()).filter(e -> Objects.equals(type, e.getType())).map(PlaceOrderTypeEnum::getDesc).findFirst().orElse(null);
    }


    private Integer type;

    private String desc;
}
