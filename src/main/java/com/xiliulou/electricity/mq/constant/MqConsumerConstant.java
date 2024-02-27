package com.xiliulou.electricity.mq.constant;

/**
 * MQ consumer constant
 *
 * @author xiaohui.song
 **/
public interface MqConsumerConstant {
    
    /**
     * 保险模块公用 consumer group
     */
    String INSURE_COMMON_CONSUMER_GROUP = "insure_common_consumer_group";
    
    /**
     * 分账模块模块公用 consumer group
     */
    String DIVISION_ACCOUNT_COMMON_CONSUMER_GROUP = "division_account_common_consumer_group";
    
    /**
     * 活动模块公用 consumer group
     */
    String ACTIVITY_COMMON_CONSUMER_GROUP = "activity_common_consumer_group";
    
    String BATTERY_CONSUMER = "battery-power-consumer";
    
    /**
     * 用户优惠券公用 consumer group
     */
    String USER_COUPON_COMMON_CONSUMER_GROUP = "user_coupon_common_consumer_group";
    
    /**
     * 商户升级 consumer group
     */
    String MERCHANT_UPGRADE_CONSUMER_GROUP = "merchant_upgrade_consumer_group";
    String MERCHANT_MODIFY_CONSUMER_GROUP = "merchant_modify_consumer_group";
    
    /**
     * 商户返利 consumer group
     */
    String BATTERY_MEMBER_CARD_MERCHANT_REBATE_GROUP = "battery_member_card_merchant_rebate_group";
}
