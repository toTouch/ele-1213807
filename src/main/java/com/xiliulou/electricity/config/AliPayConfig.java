package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-08-17:20
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
@RefreshScope
public class AliPayConfig {
    private String serverUrl;
    
    /**
     * 支付回调,后缀必须有"/"
     */
    private String payCallBackUrl;
    /**
     * 退款回调,后缀必须有"/"
     */
    private String refundCallBackUrl;
    
    /**
     * 租车套餐押金退款回调
     */
    private String carDepositRefundCallBackUrl;
    
    /**
     * 租车套餐租金退款回调
     */
    private String carRentRefundCallBackUrl;
    
    /**
     * 电池套餐退租金回调
     */
    private String batteryRentRefundCallBackUrl;
}
