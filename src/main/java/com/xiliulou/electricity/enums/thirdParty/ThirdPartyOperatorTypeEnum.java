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
    ELE_CABINET_STATUS("ELE_CABINET_STATUS", "柜机上下线");
    
    private final String type;
    
    private final String desc;
    
    ThirdPartyOperatorTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    
}
