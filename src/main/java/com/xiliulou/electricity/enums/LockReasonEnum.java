package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author maxiaodong
 * @date 2024/10/30 21:31
 * @desc
 */
@Getter
@AllArgsConstructor
public enum LockReasonEnum {
    OTHER(50006, "已过期")
    ;
    
    private final Integer code;
    
    private final String desc;
}
