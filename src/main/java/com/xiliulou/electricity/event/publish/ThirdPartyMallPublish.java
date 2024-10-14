package com.xiliulou.electricity.event.publish;

import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.event.ThirdPartyMallEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 13:45:49
 */
@Slf4j
@Component
public class ThirdPartyMallPublish {
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("THIRD-PARTY-MALL-THREAD-POOL", 3, "thirdPartyMallThread");
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public ThirdPartyMallPublish(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public void publish(ThirdPartyMallEvent event) {
        log.info("Publish third party mall event={}", event);
        
        CompletableFuture.runAsync(() -> {
            applicationEventPublisher.publishEvent(event);
        }, threadPool).exceptionally(e -> {
            log.error("ThirdPartyMallPublish ERROR!", e);
            return null;
        });
    }
}
