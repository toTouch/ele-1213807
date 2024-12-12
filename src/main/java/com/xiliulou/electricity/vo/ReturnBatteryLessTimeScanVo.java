package com.xiliulou.electricity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: ReturnBatteryLessTimeScanVo
 * @description:
 * @author: renhang
 * @create: 2024-11-06 11:45
 */
@Data
@Accessors(chain = true)
public class ReturnBatteryLessTimeScanVo {
    
    /**
     * 订单id
     */
    private String orderId;
    
    /**
     * 是否满足自主开仓条件，如果不满足，前端不会再请求 1:满足；0不满足
     */
    private Integer isSatisfySelfOpen;
    
    /**
     * 仓门
     */
    private Integer cell;
    
    /**
     * 电池是否在仓
     */
    private Integer isBatteryInCell;
    
    /**
     * 是否进入多次换电
     */
    private Integer isEnterMoreExchange;
    
    
}
