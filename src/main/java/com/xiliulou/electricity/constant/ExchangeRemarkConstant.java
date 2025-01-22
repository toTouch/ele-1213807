package com.xiliulou.electricity.constant;

/**
 * @ClassName: ExchangeRemarkConstant
 * @description:
 * @author: renhang
 * @create: 2024-11-06 09:47
 */
public class ExchangeRemarkConstant {
    

    //---- 后台自主开仓
    /**
     * 上次换电成功，电池未取走，后台自助开仓
     */
    public static final String EXCHANGE_SUCCESS_SYSTEM_SELF_CELL = "EXCHANGE_SUCCESS_SYSTEM_SELF_CELL";
    
    /**
     * 灵活续费发生套餐转换后，换电时电池转换，旧电池检测失败，后台自助开仓
     */
    public static final String FLEXIBLE_RENEWAL_SYSTEM_SELF_CELL = "FLEXIBLE_RENEWAL_SYSTEM_SELF_CELL";

    /**
     * 租电成功，后台自助开仓
     */
    public static final String RENT_SUCCESS_SYSTEM_SELF_CELL = "RENT_SUCCESS_SYSTEM_SELF_CELL";


    /**
     * 上次换电/选仓 旧仓门失败，满足取电逻辑，取走满电电池
     * 上次换电新仓门开门失败
     */
    public static final String TAKE_FULL_BATTER = "TAKE_FULL_BATTER";


    /**
     * 检测电池不在仓，前端选择开仓：即用户自主开仓
     */
    public static final String USER_SELF_OPEN_CELL = "USER_SELF_OPEN_CELL";


    /**
     * 二次扫码退电，电池在仓退电完成
     */
    public static final String TWO_SCAN_RENT_BATTERY_SUCCESS = "TWO_SCAN_RENT_BATTERY_SUCCESS";
}
