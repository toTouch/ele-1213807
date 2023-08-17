package com.xiliulou.electricity.entity;

import lombok.Data;

@Data
public class UserInfoDataEntity {

    private Long uid;
    /**
     * 激活时间
     */
    private String activeTime;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 电池序列号
     */
    private String batterySn;

    /**
     * 支付次数
     */
    private int payCount;

    /**
     * 会员卡到期时间
     */
    private Long expireTime;



}
