package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum OrderTypeEnum implements BasicEnum<Integer, String> {

    BATTERY_BUY_ORDER(1, "换电套餐购买订单"),
    CAR_BUY_ORDER(2, "租车套餐购买订单"),
    ;

    private final Integer code;

    private final String desc;
}
