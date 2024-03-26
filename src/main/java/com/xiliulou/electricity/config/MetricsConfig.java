package com.xiliulou.electricity.config;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.xiliulou.electricity.constant.MetricsConstant;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-03-26-9:58
 */
@Configuration
public class MetricsConfig {
    
    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }
    
    @Bean
    public Meter sendMsgMeter(MetricRegistry metricRegistry) {
        return metricRegistry.meter(MetricsConstant.DEVICE_REPORT_METER);
    }
    
    @Bean
    public Slf4jReporter consoleReporter(MetricRegistry metricRegistry) {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("com.example.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        // 每5秒输出一次结果
        reporter.start(30, TimeUnit.SECONDS);
        return reporter;
    }
    
}
