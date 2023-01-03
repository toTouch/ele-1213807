package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * 小程序首页UserInfo视图对象
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-22-15:59
 */
@Data
public class UserBatteryMemberCardDetailVO {

    private Long memberCardExpireTime;

    private Integer existBattery;

    public static final Integer EXIST_BATTERY = 0;
    public static final Integer NOT_EXIST_BATTERY = 1;

}
