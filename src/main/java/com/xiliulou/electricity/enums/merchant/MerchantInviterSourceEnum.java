package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邀请人来源
 */
@Getter
@AllArgsConstructor
public enum MerchantInviterSourceEnum {
    MERCHANT_INVITER_SOURCE_SHARE_ACTIVITY(1, "邀请返券"),
    MERCHANT_INVITER_SOURCE_SHARE_MONEY_ACTIVITY(2, "邀请返现"),
    MERCHANT_INVITER_SOURCE_INVITATION_ACTIVITY(3, "套餐返现"),
    MERCHANT_INVITER_SOURCE_CHANNEL_ACTIVITY(4, "渠道邀请"),
    MERCHANT_INVITER_SOURCE_MERCHANT(5, "商户邀请"),
    MERCHANT_INVITER_SOURCE_USER(6, "用户邀请"),
    ;
    
    private final Integer code;
    
    private final String desc;
    
}
