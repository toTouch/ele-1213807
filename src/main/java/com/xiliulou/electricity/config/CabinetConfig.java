package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author HeYafeng
 * @description 柜机相关配置
 * @date 2024/10/28 21:17:46
 */

@Configuration
@ConfigurationProperties(prefix = "cabinet-config")
@Data
@RefreshScope
public class CabinetConfig {
    
    private String initPassword;
}
