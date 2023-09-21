package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 17:21
 */

@Getter
@AllArgsConstructor
public enum RenewalStatusEnum implements BasicEnum<Integer, String> {

    RENEWAL_STATUS_NOT_BY_SELF(0, "不自主续费"),

    RENEWAL_STATUS_BY_SELF(1, "自主续费"),

    ;

    private final Integer code;

    private final String desc;
}
