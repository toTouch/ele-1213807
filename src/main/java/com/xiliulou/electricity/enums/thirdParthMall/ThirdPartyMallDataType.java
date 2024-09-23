package com.xiliulou.electricity.enums.thirdParthMall;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description 第三方商城推送数据类型
 * @date 2024/9/19 11:12:19
 */
@Getter
public enum ThirdPartyMallDataType {
    USER_BATTERY_MEMBER_CARD("USER_BATTERY_MEMBER_CARD", "用户换电套餐"),
    ELE_CABINET("ELE_CABINET", "换电柜"),
    USER_EXCHANGE_RECORD("USER_EXCHANGE_RECORD", "用户换电记录"),
    USER_BATTERY("USER_EXCHANGE_RECORD", "用户电池"),
    USER_INFO("USER_INFO", "用户信息");
    
    private final String code;
    
    private final String desc;
    
    ThirdPartyMallDataType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
