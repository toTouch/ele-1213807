/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/18
 */

package com.xiliulou.electricity.config;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/18 08:46
 */
public interface BasePayCallBackConfig {
    
    
    /**
     * 支付回调
     */
    String getPayCallBackUrl();
    
    /**
     * 电池押金退款回调
     */
    String getRefundCallBackUrl();
    
    /**
     * 电池套餐退租金回调
     */
    String getBatteryRentRefundCallBackUrl();
    
    
    /**
     * 租车押金退款回调
     */
    String getCarDepositRefundCallBackUrl();
    
    
    /**
     * 租车租金退款回调
     */
    String getCarRentRefundCallBackUrl();
    
}
