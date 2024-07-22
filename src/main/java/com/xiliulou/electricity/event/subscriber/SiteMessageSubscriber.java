package com.xiliulou.electricity.event.subscriber;


import com.xiliulou.electricity.event.OverdueUserRemarkEvent;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.mapper.userinfo.overdue.UserInfoOverdueRemarkMapper;
import com.xiliulou.electricity.mq.producer.SiteMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <p>
 * Description: This class is OverdueUserRemarkSubscriber!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/11
 **/
@Slf4j
@Component
public class SiteMessageSubscriber {
    
    
    private final SiteMessageProducer siteMessageProducer;
    
    public SiteMessageSubscriber(SiteMessageProducer siteMessageProducer) {
        this.siteMessageProducer = siteMessageProducer;
    }
    
    @EventListener
    public void handleSiteMessageEvent(SiteMessageEvent event) {
        if (Objects.isNull(event)){
            log.warn("Received subscription event OverdueUserRemarkEvent is null");
            return;
        }
        siteMessageProducer.sendMessage(event.toDTO());
        if (log.isDebugEnabled()){
            log.debug("An internal message has been sent to the queue : {}",event);
        }
    }
}
