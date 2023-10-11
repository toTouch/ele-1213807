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
    @Deprecated
    private Integer batteryReportCheck;

    /**
     * 小程序首页柜机列表距离值
     */
    private Double showDistance;

    /**
     * 换电流程需要特殊处理的租户
     */
    private Integer specialTenantId;

    /**
     * 电池上报电量变化diff值
     */
    private Integer powerChangeDiff;

    private Integer testFactoryTenantId;

    /**
     * 开启电量上报检测
     */
    public static final Integer OPEN_BATTERY_REPORT_CHECK = 0;

    /**
     * 关闭电量上报检测
     */
    public static final Integer CLOSE_BATTERY_REPORT_CHECK = 1;
}
