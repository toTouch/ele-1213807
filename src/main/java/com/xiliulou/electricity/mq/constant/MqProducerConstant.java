package com.xiliulou.electricity.mq.constant;

/**
 * MQ Producer constant
 *
 * @author xiaohui.song
 **/
public interface MqProducerConstant {

    /** 保险模块公用 topic */
    String INSURE_COMMON_TOPIC = "insure_common_topic";

    /** 保险模块公用 topic 下的所有 tag */
    String INSURE_COMMON_TOPIC_TAG = "*";

    /** 活动模块公用 topic */
    String ACTIVITY_COMMON_TOPIC = "activity_common_topic";

    /** 活动模块公用 topic 下的所有 tag */
    String ACTIVITY_COMMON_TOPIC_TAG = "*";

    /** 分账模块公用 topic */
    String DIVISION_ACCOUNT_COMMON_TOPIC = "division_account_common_topic";

    /** 分账模块公用 topic 下的所有 tag */
    String DIVISION_ACCOUNT_COMMON_TOPIC_TAG = "*";

    String TOPIC_BATTERY_POWER = "battery_power_topic";

    /** 运维通知 topic */
    String TOPIC_MAINTENANCE_NOTIFY = "MAINTENANCE_NOTIFY";

    /**
     * 硬件故障
     */
    String TOPIC_HARDWARE_FAILURE = "WARNING_BREAKDOWN_TOPIC";


    /** 用户优惠券公用 topic */
    String USER_COUPON_COMMON_TOPIC = "user_coupon_common_topic";

    /** 用户优惠券公用 topic 下的所有 tag */
    String USER_COUPON_COMMON_TOPIC_TAG = "*";
}
