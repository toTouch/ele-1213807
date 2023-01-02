package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-27-16:27
 */
@Data
public class UserInfoResultVO {
    //用户状态
    private String userStatus;



    //未实名认证
    public static final String STATUS_NOT_AUTH = "not_auth";
    //待审核
    public static final String STATUS_AUDIT = "audit";
    //未购买租电池套餐
    public static final String STATUS_BUY_BATERY_MEMBERCARD = "buy_battery_membercard";
    //未购买租车套餐
    public static final String STATUS_BUY_CAR_MEMBERCARD = "buy_car_membercard";
    //有电池服务费
    public static final String STATUS_BATTERY_SERVICE_FEE = "battery_service_fee";
    //租电池
    public static final String STATUS_RENT_BATTERY = "rent_battery";
    //租车
    public static final String STATUS_RENT_CAR = "rent_car";


    //已绑定电池
    public static final String STATUS_HAVE_BATTERY = "have_battery";



    public static final String YES = "YES";
    public static final String NO = "NO";
}
