package com.xiliulou.electricity;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.xiliulou.cache.redis.EnableRedis;
import com.xiliulou.clickhouse.EnableCH;
import com.xiliulou.core.http.resttemplate.EnableXllRestTemplate;
import com.xiliulou.core.sms.EnableSms;
import com.xiliulou.core.wp.EnableWeChatTemplate;
import com.xiliulou.core.xxl.EnableXllXxlJob;
import com.xiliulou.db.dynamic.annotation.EnableDynamicDataSource;
import com.xiliulou.esign.EnableEsign;
import com.xiliulou.faceid.EnableFaceid;
import com.xiliulou.feishu.EnableFeishu;
import com.xiliulou.hwiiot.EnableHuaweiIot;
import com.xiliulou.iot.EnableIot;
import com.xiliulou.mq.EnableMq;
import com.xiliulou.pay.EnableFreeDeposit;
import com.xiliulou.pay.EnablePay;
import com.xiliulou.storage.EnableStorage;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author: eclair
 * @Date: 2020/11/23 17:25
 * @Description:
 */
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
@EnableDiscoveryClient
/*@EnableCircuitBreaker*/
@EnableRedis
@EnableHuaweiIot
@EnableStorage
@EnableXllRestTemplate
@EnableDynamicDataSource
@MapperScan("com.xiliulou.**.mapper")
@EnablePay(isOpenWechatV3 = true)
@EnableXllXxlJob
@EnableSms
@EnableCH
@EnableWeChatTemplate
@EnableFeishu
@EnableFaceid
@EnableMq
@EnableFreeDeposit
@EnableEsign
@Slf4j
public class ElectricityCabinetApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElectricityCabinetApplication.class, args);

    }
}
