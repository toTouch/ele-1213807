package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MerchantWithdrawTypeEnum {
    OLD(0, "旧流程业务"),
    NEW(1, "新流程业务")
            ;
    private final Integer code;

    private final String desc;
}
