/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.config.message;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

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
    
    private String url;
    
    
    
    
}
