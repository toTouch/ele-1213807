package com.xiliulou.electricity.enums.thirdParty;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description 第三方推送数据操作类型
 * @date 2024/9/19 11:12:19
 */
@Getter
public enum ThirdPartyOperatorTypeEnum {
    ELE_CABINET_ADD("ELE_CABINET_ADD", "新增柜机"),
    ELE_CABINET_EDIT("ELE_CABINET_EDIT", "编辑柜机"),
    ELE_CABINET_STATUS("ELE_CABINET_STATUS", "柜机上下线"),
    BATTERY_ADD("BATTERY_ADD", "新增电池"),
    BATTERY_EDIT("BATTERY_EDIT", "编辑电池"),
    BATTERY_STATUS("BATTERY_STATUS", "电池状态"),
    STORE_ADD("STORE_ADD", "新增门店"),
    STORE_EDIT("STORE_EDIT", "编辑门店"),
    STORE_STATUS("STORE_STATUS", "禁启用门店");
    
    private final String type;
    
    private final String desc;
    
    ThirdPartyOperatorTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    
}
