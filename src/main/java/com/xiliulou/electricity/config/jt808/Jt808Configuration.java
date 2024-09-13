/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/9/13
 */

package com.xiliulou.electricity.config.jt808;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/13 11:25
 */
@Configuration
@ConfigurationProperties(prefix = "jt808")
@Data
@RefreshScope
public class Jt808Configuration {
    
    private Boolean close = false;
    
}
