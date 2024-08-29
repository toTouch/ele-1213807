package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: FreeDepositConfig
 * @description:
 * @author: renhang
 * @create: 2024-08-23 13:32
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "free.deposit.notify")
public class FreeDepositConfig {
    
    /**
     * 免押代扣回调 /outer/free/notified/%d/%d/%d 第一个参数为 免押渠道 FreeDepositServiceWayEnums 第二个参数为 业务类型 FreeBusinessTypeEnum 第三个参数为 租户id
     */
    private String url;
    
}

