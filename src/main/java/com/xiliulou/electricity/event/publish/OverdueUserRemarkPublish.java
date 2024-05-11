package com.xiliulou.electricity.event.publish;


import com.xiliulou.electricity.event.OverdueUserRemarkEvent;
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
@Component
public class OverdueUserRemarkPublish {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public OverdueUserRemarkPublish(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public void publish(Long uid, Integer type,Integer tenantId) {
        applicationEventPublisher.publishEvent(new OverdueUserRemarkEvent(this, uid, type,tenantId));
    }
}
