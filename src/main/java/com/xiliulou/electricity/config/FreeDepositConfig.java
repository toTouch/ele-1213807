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
     * 拍小组冻结，结果通知回调
     */
    private String pxzFreeUrl;
    
    /**
     * 蜂云冻结 结果通知回调地址
     */
    private String fyFreeUrl;
    
    /**
     * 蜂云解冻 回调地址
     */
    private String fyUnFreeUrl;
    
    /**
     * 蜂云代扣 回调地址
     */
    private String fyAuthPayUrl;
}

