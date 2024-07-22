/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/17
 */

package com.xiliulou.electricity.enums;

import lombok.Getter;

/**
 * description:支付渠道枚举
 *
 * @author caobotao.cbt
 * @date 2024/7/17 10:13
 */
@Getter
public enum PaymentChannelEnum {
    
    WECHAT("WECHAT", "微信小程序支付"),
    ALI_PAY("ALI_PAY", "支付宝小程序支付");
    
    private String code;
    
    private String desc;
    
    
    PaymentChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
