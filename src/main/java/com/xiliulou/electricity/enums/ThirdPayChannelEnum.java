package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 三方支付渠道枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum ThirdPayChannelEnum implements BasicEnum<Integer, String> {

    WEIXIN(1, "微信"),
    ;

    private final Integer code;

    private final String desc;
}
