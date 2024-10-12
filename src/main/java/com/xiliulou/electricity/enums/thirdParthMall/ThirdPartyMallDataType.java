package com.xiliulou.electricity.enums.thirdParthMall;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description 第三方商城推送数据类型
 * @date 2024/9/19 11:12:19
 */
@Getter
public enum ThirdPartyMallDataType {
    NOTIFY_MEI_TUAN_DELIVER("NOTIFY_MEI_TUAN_DELIVER", "通知美团发货"),
    PUSH_USER_BATTERY_MEMBER_CARD("PUSH_USER_BATTERY_MEMBER_CARD", "用户换电套餐"),
    PUSH_ELE_CABINET("PUSH_PUSH_ELE_CABINET", "换电柜"),
    PUSH_USER_EXCHANGE_RECORD("PUSH_USER_EXCHANGE_RECORD", "用户换电记录"),
    PUSH_USER_BATTERY("PUSH_USER_EXCHANGE_RECORD", "用户电池"),
    PUSH_USER_INFO("PUSH_USER_INFO", "用户信息");
    
    private final String code;
    
    private final String desc;
    
    ThirdPartyMallDataType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
