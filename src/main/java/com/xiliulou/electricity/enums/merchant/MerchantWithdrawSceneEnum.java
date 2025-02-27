package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MerchantWithdrawSceneEnum {
    DISTRIBUTION_REBATE(1005, "分销返佣"),
            ;
    private final Integer code;

    private final String desc;
}
