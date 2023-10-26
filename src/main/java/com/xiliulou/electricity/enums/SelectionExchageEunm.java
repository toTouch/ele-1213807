package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 选仓换电枚举
 *
 * @author zhangyongbo
 **/
@Getter
@AllArgsConstructor
public enum SelectionExchageEunm implements BasicEnum<Integer, String> {
    
    /**
     * 开启选仓换电 （0--开启 1--关闭）
     */
    ENABLE_SELECTION_EXCHANGE(0, "开启选仓换电"),
    DISABLE_SELECTION_EXCHANGE(1, "关闭选仓换电"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
