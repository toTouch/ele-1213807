package com.xiliulou.electricity.event.publish;


import com.xiliulou.electricity.event.SiteMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: This class is OverdueUserRemarkPublish!
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
public class SiteMessagePublish {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public SiteMessagePublish(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public void publish(SiteMessageEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
