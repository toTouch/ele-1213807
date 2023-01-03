package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-27-16:27
 */
@Data
public class UserInfoResultVO {

    //实名认证审核状态 -1:初始化，0：等待审核中,1：审核被拒绝,2：审核通过
    private Integer authStatus;

    private UserBatteryDetail userBatteryDetail;

    private UserCarDetail UserCarDetail;











    //用户状态
    private String userStatus;
    //套餐状态 1：租车和换电套餐都没有，2：租车和换电套餐都有，3：有换电套餐，4：有租车套餐
    private Integer membercardStatus;



    //未实名认证
    public static final String STATUS_NOT_AUTH = "not_auth";
    //待审核
    public static final String STATUS_AUDIT = "audit";
    //未购买租电池套餐
    public static final String STATUS_BUY_BATERY_MEMBERCARD = "buy_battery_membercard";
    //租电池套餐过期
    public static final String STATUS_BATERY_MEMBERCARD_expire = "battery_membercard_expire";
    //未购买租车套餐
    public static final String STATUS_BUY_CAR_MEMBERCARD = "buy_car_membercard";
    //租车套餐过期
    public static final String STATUS_CAR_MEMBERCARD_expire = "car_membercard_expire";
    //有电池服务费
    public static final String STATUS_BATTERY_SERVICE_FEE = "battery_service_fee";
    //租电池
    public static final String STATUS_RENT_BATTERY = "rent_battery";
    //租车
    public static final String STATUS_RENT_CAR = "rent_car";

    //已绑定电池
    public static final String STATUS_HAVE_BATTERY = "have_battery";



    public static final Integer YES = 0;
    public static final Integer NO = 1;

}
