package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum ExchangeReasonTypeEnum {
    
    
    OPEN_CELL_FAIL(1, "开门失败"),
    
    ;
    
    
    /**
     * code
     */
    private final Integer reasonCode;
    
    /**
     * 描述
     */
    private final String reason;
    
    
    public static Integer getReasonCodeByReason(String msg) {
        return Arrays.stream(ExchangeReasonTypeEnum.values()).filter(e -> msg.contains(e.getReason())).map(ExchangeReasonTypeEnum::getReasonCode).findFirst().orElse(null);
    }
    
}
