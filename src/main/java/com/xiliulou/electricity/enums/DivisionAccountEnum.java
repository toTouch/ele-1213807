package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: Kenneth
 * @Date: 2023/7/28 15:46
 * @Description:
 */

@Getter
@AllArgsConstructor
public enum DivisionAccountEnum implements BasicEnum<Integer, String> {

    DA_TYPE_PURCHASE(0, "分账类型-购买"),

    DA_TYPE_REFUND(1, "分账类型-退租"),

    ;

    private final Integer code;

    private final String desc;
}
