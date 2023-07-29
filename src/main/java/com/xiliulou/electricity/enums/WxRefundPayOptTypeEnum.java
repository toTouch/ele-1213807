package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信支付回调业务类型枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum WxRefundPayOptTypeEnum implements BasicEnum<String, String> {

    CAR_DEPOSIT_REFUND_CALL_BACK("CAR_DEPOSIT_REFUND_CALL_BACK", "租车套餐押金退还"),
    CAR_RENT_REFUND_CALL_BACK("CAR_DEPOSIT_REFUND_CALL_BACK", "租车套餐租金退还"),
    BATTERY_RENT_REFUND_CALL_BACK("wxRefundPayBatteryRentServiceImpl", "电池套餐租金退还"),
    ;

    private final String code;

    private final String desc;
}
