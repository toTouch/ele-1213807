package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName : PromotionFeeQueryEnum
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Getter
@AllArgsConstructor
public enum PromotionFeeQueryTypeEnum {
    
    //登录注册在3.0版本中先移除
    MERCHANT(1, "商户"),
    MERCHANT_EMPLOYEE(2, "场地员工"),
    CHANNEL_EMPLOYEE(3, "渠道员"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
