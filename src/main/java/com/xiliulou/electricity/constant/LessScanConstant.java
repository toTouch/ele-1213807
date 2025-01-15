package com.xiliulou.electricity.constant;

import lombok.Data;

/**
 * @ClassName: LessScanConstant
 * @description:
 * @author: renhang
 * @create: 2024-11-06 18:12
 */
@Data
public class LessScanConstant {
    
    
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
    
    /**
     * 是否进入取电逻辑
     */
    public static final Integer ENTER_TAKE_BATTERY = 1;
    
}
