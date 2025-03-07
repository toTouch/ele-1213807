package com.xiliulou.electricity.enums.thirdParth;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description 第三方渠道枚举
 * @date 2024/9/19 14:54:31
 * @since
 */
@Getter
public enum ThirdPartyChannelEnum {
    /**
     * 直接推送数据给美团
     */
    MEI_TUAN_RIDER_MALL(1, "美团骑手商城"),
    
    /**
     * 通过西六楼平台推送给第三方
     */
    XLL_PLAT_FORM(2, "西六楼平台");
    
    private final Integer code;
    
    private final String desc;
    
    ThirdPartyChannelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
