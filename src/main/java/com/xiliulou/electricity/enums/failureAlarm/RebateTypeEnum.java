package com.xiliulou.electricity.enums.failureAlarm;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author maxiaodong
 * @date 2024/2/26 22:13
 * @desc
 */
@Getter
@AllArgsConstructor
public enum RebateTypeEnum implements BasicEnum<Integer, String> {
    FIRST(0, "拉新"),
    
    RENEW(1, "续费"),
    
    BALANCE(2, "返现");
    
    
    private final Integer code;
    
    private final String desc;
   
}
