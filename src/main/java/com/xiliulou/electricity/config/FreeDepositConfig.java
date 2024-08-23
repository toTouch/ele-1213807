package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: FreeDepositConfig
 * @description:
 * @author: renhang
 * @create: 2024-08-23 13:32
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties("free.deposit.notify")
public class FreeDepositConfig {
    
    /**
     * 拍小组
     */
    private String pxzUrl;
    
    /**
     * 蜂云冻结 回调地址
     */
    private String fyFreeUrl;
    
    /**
     * 蜂云解冻 回调地址
     */
    private String fyUnFreeUrl;
}

