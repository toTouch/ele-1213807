package com.xiliulou.electricity.config.car;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/11/20 11:26
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties("car-rental-package.transfer")
public class CarRentalPackageDataTransferConfig {
    
    private boolean enable = false;
}
