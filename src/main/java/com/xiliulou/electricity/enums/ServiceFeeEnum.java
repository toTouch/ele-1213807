package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceFeeEnum implements BasicEnum<Integer, String> {

    BATTERY_PAUSE(0, "电池套餐暂停"),
    BATTERY_EXPIRE(1, "电池套餐过期"),
    CAR(2, "车辆");

    private final Integer code;

    private final String desc;
}
