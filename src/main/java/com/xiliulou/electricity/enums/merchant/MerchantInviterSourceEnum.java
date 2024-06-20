package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邀请人来源
 */
@Getter
@AllArgsConstructor
public enum MerchantInviterSourceEnum {
    MERCHANT_INVITER_SOURCE_USER_FOR_VO(1, "用户邀请"),
    MERCHANT_INVITER_SOURCE_MERCHANT_FOR_VO(2, "商户邀请"),
    ;
    
    private final Integer code;
    
    private final String desc;
    
}
