package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邀请人是否可被修改
 */
@Getter
@AllArgsConstructor
public enum MerchantInviterCanModifyEnum {
    MERCHANT_INVITER_CAN_MODIFY(0, "可被修改"),
    MERCHANT_INVITER_CAN_NOT_MODIFY(1, "不可被修改"),
    ;
    
    private final Integer code;
    
    private final String desc;
    
}
