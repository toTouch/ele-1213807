package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信退款状态枚举值
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum WxRefundStatusEnum implements BasicEnum<String, String> {

    SUCCESS("SUCCESS", "退款成功"),
    CLOSED("CLOSED", "退款关闭"),
    ABNORMAL("ABNORMAL", "退款异常，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，可前往【商户平台—>交易中心】，手动处理此笔退款"),
    ;

    private final String code;

    private final String desc;
}
