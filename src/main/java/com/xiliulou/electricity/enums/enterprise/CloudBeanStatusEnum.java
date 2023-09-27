package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CloudBeanStatusEnum implements BasicEnum<Integer, String> {
    NOT_RECYCLE(1, "未回收"),
    
    RECOVERED(2, "已回收"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
}
