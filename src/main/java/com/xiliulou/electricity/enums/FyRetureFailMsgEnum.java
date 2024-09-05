package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @ClassName: FyRetureFailMsgEnum
 * @description:
 * @author: renhang
 * @create: 2024-09-05 09:11
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum FyRetureFailMsgEnum {
    
    
    PAY_NOT_MONEY_FAIL("支付宝预授权转支付失败，没有可用的支付工具", "用户账户余额不足，代扣失败"),
    
    ;
    
    private final String originalMsg;
    
    private final String returnMsg;
    
    
    public static String getReturnMsg(String originalMsg) {
        return Arrays.stream(FyRetureFailMsgEnum.values()).filter(e->e.getOriginalMsg().contains(originalMsg)).map(FyRetureFailMsgEnum::getReturnMsg).findFirst().orElse(originalMsg);
    }
    
    
}
