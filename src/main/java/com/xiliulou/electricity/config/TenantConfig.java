package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author HRP
 * @date 2022/05/27 0030 16:22
 * @Description:
 */

@Configuration
@ConfigurationProperties("tenant")
@Data
@RefreshScope
public class TenantConfig {
    //租户Id
    private List<Integer> disableRobotMessageForTenantId;

    private List<Integer> notBatteryReportCheckTenantId;

}
