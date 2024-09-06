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
     * 是否进入多次换电，1是，0否
     */
    private Integer isEnterMoreExchange;
    
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
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 仓门
     */
    private Integer cell;
    
    /**
     * 订单号
     */
    private String orderId;
    
    
    private String sessionId;
    
    /**
     * 扫码和订单是否是同一个柜机，默认是1，不一样是0
     */
    private Integer isTheSameCabinet = 1;
    
    /**
     * 是否进入取电
     */
    private Integer isEnterTakeBattery;
    
    
    /**
     * 是否进入选仓换电,1是，0否
     */
    private Integer isEnterSelectCellExchange;
    
    
    /**
     * 是否进入多次换电
     */
    public static final Integer ENTER_MORE_EXCHANGE = 1;
    
    public static final Integer NOT_ENTER_MORE_EXCHANGE = 0;
    
    /**
     * 上次换电结果是成功还是失败
     */
    public static final Integer LAST_EXCHANGE_SUCCESS = 1;
    
    public static final Integer LAST_EXCHANGE_FAIL = 0;
    
    /**
     * 是否满足自主开仓
     */
    public static final Integer IS_SATISFY_SELF_OPEN = 1;
    
    public static final Integer NOT_SATISFY_SELF_OPEN = 0;
    
    /**
     * 电池是否在仓
     */
    public static final Integer BATTERY_IN_CELL = 1;
    
    public static final Integer BATTERY_NOT_CELL = 0;
    
    /**
     * 不是同一个柜机
     */
    public static final Integer NOT_SAME_CABINET = 0;
    
    public static final Integer ENTER_TAKE_BATTERY = 1;
    
    
    /**
     * 是否进入选仓换电
     */
    public static final Integer NOT_ENTER_SELECT_CELL_EXCHANGE = 0;
    
    public static final Integer ENTER_SELECT_CELL_EXCHANGE = 1;
    
    
}
