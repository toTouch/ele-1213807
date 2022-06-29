package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author HRP
 * @date 2022/03/02 14:34
 * @Description: 公共配置
 */

@Configuration
@ConfigurationProperties(prefix = "commonconfig")
@Data
@RefreshScope
public class EleCommonConfig {

    private Integer batteryReportCheck;

    /**
     * 开启电量上报检测
     */
    public static final Integer OPEN_BATTERY_REPORT_CHECK = 0;

    /**
     * 关闭电量上报检测
     */
    public static final Integer CLOSE_BATTERY_REPORT_CHECK = 1;
}
