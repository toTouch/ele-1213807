package com.xiliulou.electricity.enums.thirdParthMall;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description 第三方商城类型
 * @date 2024/9/19 14:54:31
 */
@Getter
public enum ThirdPartyMallEnum {
    MEI_TUAN_RIDER_MALL(1, "美团骑手商城");
    
    private final Integer code;
    
    private final String desc;
    
    ThirdPartyMallEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
