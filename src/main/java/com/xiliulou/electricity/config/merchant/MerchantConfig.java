package com.xiliulou.electricity.config.merchant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Ant
 * @Date 2024/5/30
 * @Description: 商户端小程序自定义配置
 **/
@Data
@RefreshScope
@Configuration
@ConfigurationProperties("hexup.merchant")
public class MerchantConfig {
    
    /**
     * 商户端小程序 appId
     */
    private String merchantAppletId;
    
    /**
     * 商户端小程序 appSecret
     */
    private String merchantAppletSecret;
    
}
