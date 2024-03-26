package com.xiliulou.electricity.service.monitor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-03-26-13:56
 */
@Component
@Data
@Slf4j
@RefreshScope
public class IotReportMetricsMonitorComponent {
    
    @Value(value = "${thread.monitor.sleep.seconds:5}")
    private Integer sleepSeconds;
    
    @Autowired
    private CollectorRegistry collectorRegistry;
    
    @Autowired
    private MetricRegistry metricRegistry;
    
    static final Gauge IOT_REPORT_COUNT_METRIC = Gauge.build().name("iotReportCountMetric").labelNames("meanRate", "oneMinuteRate", "fiveMinuteRate").help("iot report metric")
            .register();
    
    private XllThreadPoolExecutorService xllThreadPoolExecutorService = XllThreadPoolExecutors.newFixedThreadPool("iot_report_monitor_pool", 1, "iot_report_monitor");
    
    private volatile boolean shutdown = false;
    
    @PostConstruct
    public void init() {
        collectorRegistry.register(IOT_REPORT_COUNT_METRIC);
        xllThreadPoolExecutorService.execute(this::timeTaskCheckThreadPool);
    }
    
    private void timeTaskCheckThreadPool() {
        while (!shutdown) {
            SortedMap<String, Meter> meterMap = metricRegistry.getMeters();
            if (!DataUtil.mapIsUsable(meterMap)) {
                sleep();
            }
            
            meterMap.forEach((k, v) -> {
                IOT_REPORT_COUNT_METRIC.labels(String.valueOf(v.getMeanRate()), String.valueOf(v.getOneMinuteRate()), String.valueOf(v.getFiveMinuteRate())).set(v.getCount());
            });
            
            sleep();
        }
    }
    
    public void destroy() {
        shutdown = true;
    }
    
    public void sleep() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepSeconds));
        } catch (InterruptedException e) {
            log.error("IotReportMonitor error!", e);
        }
    }
    
    
}
