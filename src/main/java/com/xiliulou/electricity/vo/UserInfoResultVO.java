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
    //未缴纳押金
    public static final String STATUS_NOT_DEPOSIT = "not_deposit";
    //未购买套餐
    public static final String STATUS_BUY_MEMBERCARD = "buy_membercard";
    //未绑定电池
    public static final String STATUS_BIND_BATTERY = "bind_battery";
    //有车辆套餐未绑定车辆
    public static final String STATUS_BIND_CAR = "bind_car";
    //租车套餐过期
    public static final String STATUS_CAR_MEMBERCARD_EXPIRE = "car_membercard_expire";
    //租电池套餐过期
    public static final String STATUS_BATTERY_MEMBERCARD_EXPIRE = "battery_membercard_expire";
    //已绑定电池
    public static final String STATUS_HAVE_BATTERY = "have_battery";
    //有电池服务费
    public static final String STATUS_BATTERY_SERVICE_FEE = "battery_service_fee";


    public static final String YES = "YES";
    public static final String NO = "NO";
}
