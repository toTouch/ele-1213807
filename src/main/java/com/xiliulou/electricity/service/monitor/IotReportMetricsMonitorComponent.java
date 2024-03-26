package com.xiliulou.electricity.service.monitor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.xiliulou.core.json.JsonUtil;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    
    static final Gauge IOT_MEAN_RATE = Gauge.build().name("meanRate").labelNames("address").help("activeCount metric").register();
    
    static final Gauge IOT_ONE_MINUTE_RATE = Gauge.build().name("oneMinuteRate").labelNames("address").help("oneMinuteRate metric").register();
    
    static final Gauge IOT_FIVE_MINUTE_RATE = Gauge.build().name("fiveMinuteRate").labelNames("address").help("fiveMinuteRate metric").register();
    
    private XllThreadPoolExecutorService xllThreadPoolExecutorService = XllThreadPoolExecutors.newFixedThreadPool("iot_report_monitor_pOOL", 1, "iot_report_monitor");
    
    private volatile boolean shutdown = false;
    
    @PostConstruct
    public void init() {
        collectorRegistry.register(IOT_MEAN_RATE);
        collectorRegistry.register(IOT_ONE_MINUTE_RATE);
        collectorRegistry.register(IOT_FIVE_MINUTE_RATE);
        xllThreadPoolExecutorService.execute(this::timeTaskCheckThreadPool);
    }
    
    private void timeTaskCheckThreadPool() {
        while (!shutdown) {
            SortedMap<String, Meter> meterMap = metricRegistry.getMeters();
            if (!DataUtil.mapIsUsable(meterMap)) {
                sleep();
            }
            
            meterMap.forEach((k, v) -> {
                log.error("===============kkk====================={}", k);
                log.error("===============vvv====================={}", JsonUtil.toJson(v));
                String address = "0";
                try {
                    address = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("ele error!not found address", e);
                    return;
                }
                
                IOT_MEAN_RATE.labels(address, address).set(v.getMeanRate());
                IOT_ONE_MINUTE_RATE.labels(address, address).set(v.getOneMinuteRate());
                IOT_FIVE_MINUTE_RATE.labels(address, address).set(v.getFiveMinuteRate());
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
