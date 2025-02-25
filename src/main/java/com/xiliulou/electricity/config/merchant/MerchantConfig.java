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

    /**
     * #跳转下程序类型：developer为开发版;trial为体验版;formal为正式版;默认人为正式版
     */
    private String miniProgramState;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 跳转页面
     */
    private String page;
    
}
