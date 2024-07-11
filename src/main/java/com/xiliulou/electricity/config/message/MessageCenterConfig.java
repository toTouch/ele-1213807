package com.xiliulou.electricity.config.message;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author maxiaodong
 * @date 2024/6/19 16:57
 * @desc
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties("message-center")
public class MessageCenterConfig {
    
    /**
     * 消息中心地址
     */
    private String url;
    
    /**
     * 短信提醒模板code
     */
    private String lowNoteNoticeMessageTemplateCode;
}
