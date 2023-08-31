package com.xiliulou.electricity.config.feishu;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * 飞书配置类
 *
 * @author xiaohui.song
 **/
@Data
@RefreshScope
@Configuration
@ConfigurationProperties("feishu")
public class FeishuConfig implements Serializable {

    private String saasFeishuRobotUrl;

    private boolean openMsg = Boolean.FALSE;

}
