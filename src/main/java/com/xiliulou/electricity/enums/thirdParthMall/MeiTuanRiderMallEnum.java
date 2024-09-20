package com.xiliulou.electricity.enums.thirdParthMall;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 18:24:19
 */
@Getter
public enum MeiTuanRiderMallEnum {
    ENABLE_MEI_TUAN_RIDER_MALL(0, "开启美团骑手商城"),
    DISABLE_MEI_TUAN_RIDER_MALL(1, "关闭美团骑手商城");
    
    
    private final Integer code;
    
    private final String desc;
    
    MeiTuanRiderMallEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
