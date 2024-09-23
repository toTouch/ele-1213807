package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: ExchangeConfig
 * @description:
 * @author: renhang
 * @create: 2024-07-19 10:08
 */
@Configuration
@ConfigurationProperties(prefix = "exchange")
@Data
@RefreshScope
public class ExchangeConfig {
    
    /**
     * 3分钟内再次扫码换电
     */
    private String scanTime;
}
