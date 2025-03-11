package com.xiliulou.electricity.event.publish;


import com.xiliulou.electricity.event.LostUserActivityDealEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


/**
 * 流失用户套餐购买成功后活动处理
 * @author maxiaodong
 * @description:
 * @date 2024/1/31 20:36
 */
@Slf4j
@Component
public class LostUserActivityDealPublish {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public LostUserActivityDealPublish(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public void publish(Long uid, Integer unfreezeUserActivityType, Integer tenantId, String orderId) {
        log.info("Publish lost user activity deal event : uid[{}] unfreezeUserActivityType[{}] orderId[{}]", uid, unfreezeUserActivityType, orderId);
        applicationEventPublisher.publishEvent(new LostUserActivityDealEvent(this, uid, unfreezeUserActivityType, tenantId, orderId));
    }
}
