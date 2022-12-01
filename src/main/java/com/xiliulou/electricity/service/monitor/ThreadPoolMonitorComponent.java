package com.xiliulou.electricity.service.monitor;

import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : eclair
 * @date : 2022/11/30 14:51
 */
@Component
@Data
@RefreshScope
public class ThreadPoolMonitorComponent {
    
    @Value(value = "${thread.monitor.sleep.seconds:5}")
    private Integer sleepSeconds;
    
    @Autowired
    CollectorRegistry collectorRegistry;
    
    XllThreadPoolExecutorService xllThreadPoolExecutorService = XllThreadPoolExecutors.newFixedThreadPool(
            "Monitor_POOL", 1, "monitor");
    
    static final Gauge PROCESSING_TASK = Gauge.build().name("activeCount").labelNames("poolName", "coreSize", "maxSize")
            .help("activeCount metric").register();
    
    static final Gauge QUEUE_WAIT_PROCESS = Gauge.build().name("queueSize")
            .labelNames("poolName", "coreSize", "maxSize").help("queueSize metric").register();
    
    static final Gauge COMPLETE_TASK = Gauge.build().name("completedTask").labelNames("poolName", "coreSize", "maxSize")
            .help("completedTask metric").register();
    
    static final Gauge POOL_SIZE = Gauge.build().name("poolSize").labelNames("poolName", "coreSize", "maxSize")
            .help("poolSize metric").register();
    
    private volatile boolean shutdown = false;
    
    
    @PostConstruct
    public void init() {
        collectorRegistry.register(PROCESSING_TASK);
        collectorRegistry.register(QUEUE_WAIT_PROCESS);
        collectorRegistry.register(COMPLETE_TASK);
        collectorRegistry.register(POOL_SIZE);
        xllThreadPoolExecutorService.execute(this::timeTaskCheckThreadPool);
    }
    
    
    private void timeTaskCheckThreadPool() {
        while (!shutdown) {
            Map<String, XllThreadPoolExecutorService> runningExe = XllThreadPoolExecutors.getRunningExe();
            
            if (!DataUtil.mapIsUsable(runningExe)) {
                sleep();
            }
            
            runningExe.forEach((poolName, service) -> {
                PROCESSING_TASK.labels(poolName, String.valueOf(service.getCorePoolSize()),
                        String.valueOf(service.getMaximumPoolSize())).set(service.getActiveCount());
                QUEUE_WAIT_PROCESS.labels(poolName, String.valueOf(service.getCorePoolSize()),
                        String.valueOf(service.getMaximumPoolSize())).set(service.getQueueSize());
                COMPLETE_TASK.labels(poolName, String.valueOf(service.getCorePoolSize()),
                        String.valueOf(service.getMaximumPoolSize())).set(service.getCompletedTaskCount());
                POOL_SIZE.labels(poolName, String.valueOf(service.getCorePoolSize()),
                        String.valueOf(service.getMaximumPoolSize())).set(service.getPoolSize());
            });
    
            sleep();
        }
        
    }
    
    public void shutdown() {
        shutdown = true;
    }
    
    public void sleep() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepSeconds));
        } catch (InterruptedException e) {
        
        }
    }
}
