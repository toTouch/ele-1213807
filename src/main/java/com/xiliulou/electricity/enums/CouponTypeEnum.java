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
    BATTERY_BUY_PACKAGE(2, "购买套餐（电）"),
    CAR_BUY_PACKAGE(3, "购买套餐（车）"),
    INVITE_COUPON_ACTIVITIES(4, "邀请返券活动"),
    REGISTER_ACTIVITIES(5, "注册活动"),
    COUPON_PACKAGE(6, "优惠券包"),
    ;

    private final Integer code;

    private final String desc;
}
