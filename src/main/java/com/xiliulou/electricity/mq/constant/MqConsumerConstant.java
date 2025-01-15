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
    
    /**
     * 分账订单消费组
     */
    String PROFIT_SHARING_ORDER_GROUP = "PROFIT_SHARING_ORDER_GROUP";
    
    /**
     * 电池回收消费组
     */
    String BATTERY_RECYCLE_GROUP = "BATTERY_RECYCLE_GROUP";

    /**
     * 电池回收消费组
     */
    String CABINET_NORMAL_RESULT_GROUP = "CABINET_NORMAL_RESULT_GROUP";
    
    /**
     * 分账订单退款消费组
     */
    String PROFIT_SHARING_ORDER_REFUND_GROUP = "PROFIT_SHARING_ORDER_REFUND_GROUP";
    
    /**
     * 电池电压电流变化 consumer group
     */
    String BATTERY_CHARGE_ATTR_CHANGE_GROUP = "battery_charge_attr_change_group";
    
    
    /**
     * 消息发送group
     */
    String MESSAGE_SEND_SAAS_GROUP = "message_send_saas_group";
    
    
    
    /**
     * 支付宝退款异步通知 consumer group
     */
    String ALIPAY_REFUND_NOTIFY_GROUP = "alipay_refund_notify_group";
    
    /**
     * 使用同一topic,不同tag,为保证订阅消息一致性，免押、解冻、代扣使用不同消费者组
     */
    String FREE_DEPOSIT_CONSUMER_GROUP = "free_deposit_group";
    String UN_FREE_DEPOSIT_CONSUMER_GROUP = "un_free_deposit_group";
    String AUTH_PAY_CONSUMER_GROUP = "auth_pay_group";

    /**
     * 用户设备在线日志 consumer group
     */
    String USER_DEVICE_STATUS_CONSUMER_GROUP = "user_device_status_consumer";
    
    /**
     * 分期代扣相关group
     */
    String INSTALLMENT_DEDUCT_GROUP = "installment_deduct_group";
    
    /**
     * 分期代扣处理成失败状态相关group
     */
    String INSTALLMENT_DEDUCT_FAIL_GROUP = "installment_deduct_fail_group";
    
    /**
     * 分期代扣续费套餐相关group
     */
    String INSTALLMENT_DEDUCT_NOTIFY_GROUP = "installment_deduct_notify_group";
}
