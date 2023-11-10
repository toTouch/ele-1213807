package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/15 14:36
 */

@Getter
@AllArgsConstructor
public enum BatteryMemberCardBusinessTypeEnum implements BasicEnum<Integer, String> {

    BUSINESS_TYPE_BATTERY(0, "换电套餐"),

    BUSINESS_TYPE_CAR_BATTERY(1, "车电一体套餐"),

    BUSINESS_TYPE_ENTERPRISE_BATTERY(2, "企业渠道换电套餐"),

    ;

    private final Integer code;

    private final String desc;

}
