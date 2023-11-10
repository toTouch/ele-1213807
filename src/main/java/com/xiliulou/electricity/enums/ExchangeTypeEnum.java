package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 换电类型
 *
 * @author zhangyongbo
 **/
@Getter
@AllArgsConstructor
public enum ExchangeTypeEnum implements BasicEnum<Integer, String> {
    
    /**
     * 换电类型
     */
    NORMAL_EXCHANGE(2, "正常换电"),
    OFFLINE_EXCHANGE(3, "离线换电"),
    BLUETOOTH_EXCHANGE(4, "蓝牙换电"),
    SELECTION_EXCHANGE(5, "选仓换电"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
