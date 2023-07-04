package com.xiliulou.electricity.enums.car;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租车套餐类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum CarRentalPackageTypeEnum {

    CAR(1, "单车"),
    CAR_BATTERY(1, "车电一体"),
    ;

    private final Integer code;

    private final String desc;
}
