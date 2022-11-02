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
@ConfigurationProperties(prefix = "ota-path")
@Data
@RefreshScope
public class EleIotOtaPathConfig {
    /**
     * 核心板oss路径
     */
    private String otaPath;
}
