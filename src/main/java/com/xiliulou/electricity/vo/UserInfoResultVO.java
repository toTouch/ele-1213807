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
    //电池名称
    private String batteryName;
    private Double batteryPower;


    //未实名认证
    public static final String STATUS_NOT_AUTH = "not_auth";
    //待审核
    public static final String STATUS_AUDIT = "audit";
    //未购买套餐
    public static final String STATUS_BUY_MEMBERCARD = "buy_membercard";
    //未绑定电池
    public static final String STATUS_BIND_BATTERY = "bind_battery";
    //有车辆套餐未绑定车辆
    public static final String STATUS_BIND_CAR = "bind_car";
    /**
     * 用户名下有车辆套餐有电池套餐，但是无车无电池
     * 先出现检测到您未绑定车辆     扫码租车
     * 后出现检测到您未租电        扫码租电
     */
    public static final String STATUS_RENT_BATTERY = "rent_battery";
    public static final String STATUS_RENT_CAR = "rent_car";
    //有电池服务费
    public static final String STATUS_BATTERY_SERVICE_FEE = "battery_service_fee";
}
