package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: Kenneth
 * @Date: 2023/7/29 16:33
 * @Description:
 */

@Getter
@AllArgsConstructor
public enum ActivityEnum implements BasicEnum<Integer, String> {

    INVITATION_CRITERIA_LOGON(1, "邀请标准-登录注册"),

    INVITATION_CRITERIA_REAL_NAME(2, "邀请标准-实名认证"),

    INVITATION_CRITERIA_BUY_PACKAGE(3, "邀请标准-购买套餐"),

    ;

    private final Integer code;

    private final String desc;
}
