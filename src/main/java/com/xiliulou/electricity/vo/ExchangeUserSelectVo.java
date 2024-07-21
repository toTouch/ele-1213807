package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ExchangeUserSelectVo
 * @description:
 * @author: renhang
 * @create: 2024-07-19 11:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeUserSelectVo {
    
    /**
     * 上一次换电是否成功,1为上次成功，0失败
     */
    private Integer lastExchangeIsSuccess;
    
    /**
     * 是否满足自主开仓条件，如果不满足，前端不会再请求 1:满足；0不满足
     */
    private Integer isSatisfySelfOpen;
    
    /**
     * 电池是否在仓，1在。0否
     */
    private Integer isBatteryInCell;
    
    /**
     * 仓门
     */
    private Integer cell;
    
    private String sessionId;
    
    
    public static final Integer LAST_EXCHANGE_SUCCESS = 1;
    
    public static final Integer LAST_EXCHANGE_FAIL = 0;
    
    public static final Integer IS_SATISFY_SELF_OPEN = 1;
    
    public static final Integer NOT_SATISFY_SELF_OPEN = 0;
    
    
    public static final Integer BATTERY_IN_CELL = 1;
    
    public static final Integer BATTERY_NOT_CELL = 0;
    
    
}
