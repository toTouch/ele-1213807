package com.xiliulou.electricity.mq.constant;

/**
 * MQ Producer constant
 *
 * @author xiaohui.song
 **/
public interface MqProducerConstant {
    
    /**
     * 保险模块公用 topic
     */
    String INSURE_COMMON_TOPIC = "insure_common_topic";
    
    /**
     * 保险模块公用 topic 下的所有 tag
     */
    String INSURE_COMMON_TOPIC_TAG = "*";
    
    /**
     * 活动模块公用 topic
     */
    String ACTIVITY_COMMON_TOPIC = "activity_common_topic";
    
    /**
     * 活动模块公用 topic 下的所有 tag
     */
    String ACTIVITY_COMMON_TOPIC_TAG = "*";
    
    /**
     * 分账模块公用 topic
     */
    String DIVISION_ACCOUNT_COMMON_TOPIC = "division_account_common_topic";
    
    /**
     * 分账模块公用 topic 下的所有 tag
     */
    String DIVISION_ACCOUNT_COMMON_TOPIC_TAG = "*";
    
    String TOPIC_BATTERY_POWER = "battery_power_topic";
    
    /**
     * 运维通知 topic
     */
    String TOPIC_MAINTENANCE_NOTIFY = "MAINTENANCE_NOTIFY";
    
    /**
     * 硬件故障
     */
    String TOPIC_HARDWARE_FAILURE = "WARNING_BREAKDOWN_TOPIC";
    
    /**
     * 新硬件故障
     */
    String TOPIC_FAILURE_WARNING_BREAKDOWN = "FAILURE_WARNING_BREAKDOWN_TOPIC";
    
    String FAULT_FAILURE_WARNING_BREAKDOWN = "FAULT_WARNING_BREAKDOWN_TOPIC";
    
    
    /**
     * 用户优惠券公用 topic
     */
    String USER_COUPON_COMMON_TOPIC = "user_coupon_common_topic";
    
    /**
     * 用户优惠券公用 topic 下的所有 tag
     */
    String USER_COUPON_COMMON_TOPIC_TAG = "*";
    
    String ENTERPRISE_USER_COST_RECORD_TOPIC = "ENTERPRISE_USER_COST_RECORD_TOPIC";
    
    /**
     * 商户升级topic
     */
    String MERCHANT_UPGRADE_TOPIC = "merchant_upgrade_topic";
    /**
     * 商户升级后重新计算返利
     */
    String MERCHANT_MODIFY_TOPIC = "merchant_modify_topic";
    
    
    /**
     * 商户返利
     */
    String BATTERY_MEMBER_CARD_MERCHANT_REBATE_TOPIC = "battery_member_card_merchant_rebate_topic";
    
    /**
     * <p>
     * Description:
     * </p>
     */
    String USER_OPERATION_RECORD_LOG = "USER_OPERATION_RECORD_LOG";
    
    /**
     * 电池电压电流变化
     */
    String BATTERY_CHARGE_ATTR_CHANGE_TOPIC = "battery_charge_attr_change_topic";
    
    
    /**
     * 支付宝退款结果异步通知
     */
    String ALIPAY_REFUND_NOTIFY_TOPIC = "alipay_refund_notify_topic";
    
    /**
     * 站内信所在新项目主题
     */
    String AUX_MQ_TOPIC_NAME="XILIULOU_SAAS_AUX_TOPIC";
    
    /**
     * 站内信所在新项目主题TAG
     */
    String MQ_TOPIC_SITE_MESSAGE_TAG_NAME="SITE_MESSAGE";
}
