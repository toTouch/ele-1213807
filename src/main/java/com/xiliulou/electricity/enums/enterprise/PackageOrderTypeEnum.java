package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/25 11:14
 */

@Getter
@AllArgsConstructor
public enum PackageOrderTypeEnum implements BasicEnum<Integer, String> {

    PACKAGE_ORDER_TYPE_NORMAL(0, "普通订单"),

    PACKAGE_ORDER_TYPE_ENTERPRISE(1, "企业渠道订单"),

    ;

    private final Integer code;

    private final String desc;


}
