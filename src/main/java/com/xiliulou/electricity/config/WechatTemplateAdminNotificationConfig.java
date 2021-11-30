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
@ConfigurationProperties("wechattemplateadminnotification")
@Data
@RefreshScope
public class WechatTemplateAdminNotificationConfig {
    /**
     * 频率(分钟)
     */
    private String frequency;
    /**
     * 过期时间(小时)
     */
    private String expirationTime;
    /**
     * 模板Id
     */
    private String templateId;
}
