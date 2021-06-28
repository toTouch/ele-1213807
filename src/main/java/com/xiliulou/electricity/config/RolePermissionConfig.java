package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author LXC
 * @date 2021/6/23 0030 16:22
 * @Description:
 */

@Configuration
@ConfigurationProperties("userrole")
@Data
@RefreshScope
public class RolePermissionConfig {
    //运营商
    private List<Long> operator;
    //加盟商
    private List<Long> alliance;
    //门店
    private List<Long> shop;
}
