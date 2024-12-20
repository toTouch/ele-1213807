package com.xiliulou.electricity.constant;

/**
 * @Description 套餐常量
 * @Author: SongJP
 * @Date: 2024/6/28 9:54
 */
public interface BatteryMemberCardConstants {
    
    Integer MAX_BATTERY_MEMBER_CARD_NUM = 300;
    
    Integer MAX_COUPON_NUM = 6;
    
    Integer MAX_USER_INFO_GROUP_NUM = 10;
    
    /**
     * 不分型号
     */
    String REGARDLESS_OF_MODEL = "0";
    
    /**
     * 用户端查询套餐与用户的用户分组是否匹配
     */
    Integer CHECK_USERINFO_GROUP_USER = 0;
    
    /**
     * 后台查询套餐与用户的用户分组是否匹配
     */
    Integer CHECK_USERINFO_GROUP_ADMIN = 1;
}
