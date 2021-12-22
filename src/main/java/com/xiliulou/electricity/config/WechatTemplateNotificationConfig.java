package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author Hardy
 * @date 2021/11/26 19:00
 * @mood
 */
@Configuration
@ConfigurationProperties("wechat-template-notification")
@Data
@RefreshScope
public class WechatTemplateNotificationConfig {
    /**
     * 电池超时提醒频率(分钟)
     */
    private String batteryTimeoutFrequency;
    /**
     * 过期时间(小时)
     */
    private String expirationTime;
    /**
     * 低电量提醒频率（分钟）
     */
    private String lowBatteryFrequency;
    /**
     * 提醒电量(百分比 63)
     */
    private String batteryLevel;
}
