/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.config.message;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/27 09:42
 */
@Configuration
@ConfigurationProperties(prefix = "message-center")
@Data
@RefreshScope
public class MessageCenterConfig {
    
    /**
     * 消息中心url
     */
    private String url;
    
    /**
     * 消息类型对应的消息中心模版配置</br>
     * <p>
     * key {@link SendMessageTypeEnum#type}
     * </p>
     */
    private Map<Integer, String> messageTemplateCode = new HashMap<>();
    
    
}
