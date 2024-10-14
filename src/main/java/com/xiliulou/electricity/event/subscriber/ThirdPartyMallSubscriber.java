package com.xiliulou.electricity.event.subscriber;


import com.xiliulou.electricity.event.ThirdPartyMallEvent;
import com.xiliulou.electricity.mq.producer.ThirdPartyMallProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 15:27:27
 */
@Slf4j
@Component
public class ThirdPartyMallSubscriber {
    
    
    private final ThirdPartyMallProducer thirdPartyMallProducer;
    
    public ThirdPartyMallSubscriber(ThirdPartyMallProducer thirdPartyMallProducer) {
        this.thirdPartyMallProducer = thirdPartyMallProducer;
    }
    
    @EventListener
    public void handleThirdPartyMallEvent(ThirdPartyMallEvent event) {
        if (Objects.isNull(event)) {
            log.warn("ThirdPartyMallSubscriber warn, thirdPartyMallEvent is null");
            return;
        }
        thirdPartyMallProducer.sendMessage(event);
        if (log.isDebugEnabled()) {
            log.debug("A thirdPartyMall message has been sent to the queue : {}", event);
        }
    }
}
