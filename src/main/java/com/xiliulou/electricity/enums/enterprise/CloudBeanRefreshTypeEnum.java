package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CloudBeanRefreshTypeEnum implements BasicEnum<Integer, String> {
    CLOUD_BEAN_DETAIL(0, "详情"),

    CLOUD_BEAN_OVERVIEW(1, "总览")
    ;
    
    private final Integer code;
    
    private final String desc;
}
