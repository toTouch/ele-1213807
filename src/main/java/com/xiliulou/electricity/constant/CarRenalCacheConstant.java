package com.xiliulou.electricity.constant;

/**
 * 租车相关的缓存常量池
 *
 * @author xiaohui.song
 **/
public interface CarRenalCacheConstant {

    /** 租车套餐缓存KEY，占位符：套餐ID */
    String CAR_RENAL_PACKAGE_ID_KEY = "car_renal_package_id:%s";

    /** 租车套餐购买缓存KEY，占位符：用户ID */
    String CAR_RENAL_PACKAGE_BUY_UID_KEY = "car_renal_package_buy_uid:%s";

    /** 租车套餐会员期限缓存KEY，占位符：租户ID、用户ID */
    String CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY = "car_rental_package_member_term_tenant_uid:%s:%s";
    
    /** 租车套餐生成免押订单缓存KEY，占位符：用户ID */
    String CAR_RENAL_PACKAGE_CREATE_FREE_ORDER_UID_KEY = "car_renal_package_create_free_order_uid:%s";
    
    /** 租车套餐，退租申请单缓存KEY，占位符：用户ID */
    String CAR_RENAL_PACKAGE_REFUND_RENT_ORDER_UID_KEY = "car_renal_package_refund_rent_order_uid:%s";
    
    /** 租车套餐，退押申请单缓存KEY，占位符：用户ID */
    String CAR_RENAL_PACKAGE_REFUND_DEPOSIT_ORDER_UID_KEY = "car_renal_package_refund_deposit_order_uid:%s";
    
    
}
