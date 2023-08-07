package com.xiliulou.electricity.mq.constant;

/**
 * MQ consumer constant
 *
 * @author xiaohui.song
 **/
public interface MqConsumerConstant {

    /** 保险模块公用 consumer group */
    String INSURE_COMMON_CONSUMER_GROUP = "insure_common_consumer_group";

    /** 分账模块模块公用 consumer group */
    String DIVISION_ACCOUNT_COMMON_CONSUMER_GROUP = "division_account_common_consumer_group";

    /** 活动模块公用 consumer group */
    String ACTIVITY_COMMON_CONSUMER_GROUP = "activity_common_consumer_group";

    String BATTERY_CONSUMER = "battery-power-consumer";

    /** 用户优惠券公用 consumer group */
    String USER_COUPON_COMMON_CONSUMER_GROUP = "user_coupon_common_consumer_group";

}
