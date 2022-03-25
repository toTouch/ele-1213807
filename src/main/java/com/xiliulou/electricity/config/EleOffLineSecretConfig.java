package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author HRP
 * @date 2022/03/02 14:34
 * @Description: 离线换电秘钥配置
 */

@Configuration
@ConfigurationProperties(prefix = "offlinesecret")
@Data
@RefreshScope
public class EleOffLineSecretConfig {

    private String secret;

    private Long step;
}
