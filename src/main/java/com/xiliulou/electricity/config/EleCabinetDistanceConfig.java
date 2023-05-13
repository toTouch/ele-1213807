package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-13-17:58
 */
@Configuration
@ConfigurationProperties(prefix = "ele-distance")
@Data
@RefreshScope
public class EleCabinetDistanceConfig {

    private Double showDistance;
}
