package com.xiliulou.electricity.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/11
 **/
@Getter
public class LostUserActivityDealEvent extends ApplicationEvent {
    
    private final Long uid;
    
    /**
     * 是否解绑用户身上的活动信息：0：是，1：否
     */
    private final Integer unfreezeUserActivityType;
    
    private final Integer tenantId;
    
    private final String orderId;
    
    public LostUserActivityDealEvent(Object source, Long uid, Integer unfreezeUserActivityType, Integer tenantId, String orderId) {
        super(source);
        this.uid = uid;
        this.unfreezeUserActivityType = unfreezeUserActivityType;
        this.tenantId = tenantId;
        this.orderId = orderId;
    }
}
