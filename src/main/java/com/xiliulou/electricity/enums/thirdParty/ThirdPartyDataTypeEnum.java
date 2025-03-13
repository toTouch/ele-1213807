package com.xiliulou.electricity.enums.thirdParty;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description 第三方推送数据类型
 * @date 2024/9/19 11:12:19
 */
@Getter
public enum ThirdPartyDataTypeEnum {
    PUSH_BATTERY_MEMBER_CARD_ORDER("PUSH_BATTERY_MEMBER_CARD_ORDER", "换电套餐订单"),
    PUSH_BATTERY_EXCHANGE_ORDER("PUSH_BATTERY_EXCHANGE_ORDER", "换电订单"),
    PUSH_BATTERY_RENT_ORDER("PUSH_BATTERY_RENT_ORDER", "租电订单"),
    PUSH_ELE_CABINET("PUSH_ELE_CABINET", "换电柜"),
    
    PUSH_STORE("PUSH_STORE","门店");
    
    private final String code;
    
    private final String desc;
    
    ThirdPartyDataTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
