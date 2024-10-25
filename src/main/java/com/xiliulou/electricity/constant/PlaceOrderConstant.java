package com.xiliulou.electricity.constant;

import java.math.BigDecimal;

/**
 * @Description 押金、套餐、保险购买下单支付功能相关常量
 * @Author: SongJP
 * @Date: 2024/10/25 17:29
 */
public interface PlaceOrderConstant {
    
    /**
     * 业务类型--押金缴纳
     */
    Integer PLACE_ORDER_DEPOSIT = 1;
    
    /**
     * 业务类型--套餐购买
     */
    Integer PLACE_ORDER_MEMBER_CARD = 2;
    
    /**
     * 业务类型--保险购买
     */
    Integer PLACE_ORDER_INSURANCE = 4;
    
    /**
     * 业务类型--套餐及保险购买
     */
    Integer PLACE_ORDER_MEMBER_CARD_AND_INSURANCE = 6;
    
    /**
     * 业务类型--押金、套餐及保险购买
     */
    Integer PLACE_ORDER_ALL = 7;
    
    /**
     * 业务来源 0--后台 1--用户端
     */
    Integer BUSINESS_SOURCE_BACKSTAGE = 0;
    
    /**
     * 业务来源 0--后台 1--用户端
     */
    Integer BUSINESS_SOURCE_USER = 1;
    
    /**
     * 最小支付金额
     */
    BigDecimal AMOUNT_MIN = BigDecimal.valueOf(0.01);
    
    /**
     * 线上支付
     */
    Integer ONLINE_PAYMENT = 0;
    
    /**
     * 线下支付
     */
    Integer OFFLINE_PAYMENT = 1;
}
