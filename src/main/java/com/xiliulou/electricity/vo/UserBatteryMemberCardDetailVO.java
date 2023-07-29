package com.xiliulou.electricity.vo;

import lombok.Data;

@Data
public class UserBatteryMemberCardDetailVO {

    private Long memberCardExpireTime;

    private Integer existBattery;

    public static final Integer EXIST_BATTERY = 0;
    public static final Integer NOT_EXIST_BATTERY = 1;

}
