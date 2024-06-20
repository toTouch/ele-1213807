package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author HeYafeng
 * @description 参与活动成功类型枚举
 * @date 2024/5/9 09:54:18
 */
@Getter
@AllArgsConstructor
public enum UserInfoActivitySourceEnum {
    SUCCESS_NO(0, "无参与成功的活动"),
    SUCCESS_SHARE_ACTIVITY(1, "邀请返券"),
    SUCCESS_SHARE_MONEY_ACTIVITY(2, "邀请返现"),
    SUCCESS_INVITATION_ACTIVITY(3, "套餐返现"),
    SUCCESS_CHANNEL_ACTIVITY(4, "渠道邀请"),
    SUCCESS_MERCHANT_ACTIVITY(5, "商户邀请");
    
    private Integer code;
    
    private String desc;
}
