package com.xiliulou.electricity;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.xiliulou.cache.redis.EnableRedis;
import com.xiliulou.core.http.resttemplate.EnableXllRestTemplate;
import com.xiliulou.core.sms.EnableSms;
import com.xiliulou.db.dynamic.annotation.EnableDynamicDataSource;
import com.xiliulou.core.xxl.EnableXllXxlJob;
import com.xiliulou.iot.EnableIot;
import com.xiliulou.pay.EnablePay;
import com.xiliulou.storage.EnableStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: eclair
 * @Date: 2020/11/23 17:25
 * @Description:
 */
@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableRedis
@EnableIot
@EnableStorage
@EnableXllRestTemplate
@EnableDynamicDataSource
@MapperScan("com.xiliulou.**.mapper")
@EnablePay
@EnableXllXxlJob
@EnableSms
public class ElectricityCabinetApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElectricityCabinetApplication.class, args);

    }
}
