package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: Kenneth
 * @Date: 2023/7/28 14:19
 * @Description:
 */

@Getter
@AllArgsConstructor
public enum PackageTypeEnum implements BasicEnum<Integer, String> {

    PACKAGE_TYPE_BATTERY(1, "换电套餐类型"),

    PACKAGE_TYPE_CAR_RENTAL(2, "租车套餐类型"),

    PACKAGE_TYPE_CAR_BATTERY(3, "车电一体套餐类型"),

    ;

    private final Integer code;

    private final String desc;

}
