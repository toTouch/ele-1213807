package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author zgw
 * @date 2022/8/17 16:18
 * @mood
 */
@Configuration
@ConfigurationProperties(prefix = "ota-url")
@Data
@RefreshScope
public class EleIotOtaUrlConfig {
    /**
     * 子版url
     */
    private String subUrl;
    /**
     * 核心板url
     */
    private String coreUrl;
}
