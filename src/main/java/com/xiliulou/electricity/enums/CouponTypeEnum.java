package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发券类型枚举
 */
@AllArgsConstructor
@Getter
@SuppressWarnings("all")
public enum CouponTypeEnum {
    
    BATCH_RELEASE(1, "批量发放"),
    BUY_PACKAGE(2, "购买套餐"),
    INVITE_COUPON_ACTIVITIES(3, "邀请返券活动"),
    REGISTER_ACTIVITIES(4, "注册活动"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
