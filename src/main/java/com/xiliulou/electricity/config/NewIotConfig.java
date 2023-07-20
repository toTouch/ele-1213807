package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author : eclair
 * @date : 2023/7/11 10:16
 */
@Configuration
@ConfigurationProperties("new-iot-gray")
@Data
@RefreshScope
public class NewIotConfig {
    /**
     * 需要灰度的设备列表
     */
    private List<String> deviceNames;
    /**
     * 是否需要全量打开
     */
    private Boolean isOpenAll;
}
