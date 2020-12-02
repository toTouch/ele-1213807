package com.xiliulou.electricity.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-02 10:45
 **/
@Configuration
@ConfigurationProperties("xiliulou.wxconfig")
@RefreshScope
@Data
public class WeiXinPayConfig {
    //支付回调地址
    private String payNotifyUrl;
    //退款回调地址
    private String refundNotifyUrl;
    //商户证书地址
    private String apiclientCertPath;
    //微信小程序订单有效时间 //其他类型支付可以再加  (默认走配置,也可自定义)
    private Long weChatProExpirceTime;

}
