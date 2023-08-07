package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租赁套餐类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum RentalPackageTypeEnum implements BasicEnum<Integer, String> {

    /** 单电的类型，此处仅仅是订单，尚未和所有的业务线拉平，所以尚不能用 */
    BATTERY(0, "单电"),
    CAR(1, "单车"),
    CAR_BATTERY(2, "车电一体"),
    ;

    private final Integer code;

    private final String desc;
}
