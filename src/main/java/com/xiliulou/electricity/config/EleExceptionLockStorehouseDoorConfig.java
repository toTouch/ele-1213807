package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author HRP
 * @date 2022/2/23 14:34
 * @Description: 换电柜异常仓门锁仓
 */

@Configuration
@ConfigurationProperties(prefix = "doorlock")
@Data
@RefreshScope
public class EleExceptionLockStorehouseDoorConfig {

    /**
     * 是否开启异常锁仓功能 0:启用 1：不启用
     */
    private Integer isOpenLock;

    /**
     * 启用状态
     */
    public static final Integer OPEN_LOCK = 0;

    /**
     * 关闭状态
     */
    public static final Integer CLOSE_LOCK=1;

}
