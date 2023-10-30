package com.xiliulou.electricity.mq.producer;

import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户优惠券的 Producer
 *
 * @author xiaohui.song
 **/
@Deprecated
@Slf4j
@Component
public class UserCouponProducer {
    
    @Resource
    private RocketMqService rocketMqService;
    
    public void sendAsyncMessage(String topic, String tag, String message) {
        log.info("UserCouponProducer.sendAsyncMessage, topic is {}, tag is {}, message is {}", topic, tag, message);
        rocketMqService.sendAsyncMsg(topic, message, tag);
    }
    
    public void sendAsyncMessage(String topic, String message) {
        log.info("UserCouponProducer.sendAsyncMessage, topic is {}, message is {}", topic, message);
        rocketMqService.sendAsyncMsg(topic, message);
    }
    
    public void sendAsyncMessage(String message) {
        log.info("UserCouponProducer.sendAsyncMessage, message is {}", message);
        rocketMqService.sendAsyncMsg(MqProducerConstant.USER_COUPON_COMMON_TOPIC, message);
    }
    
    public void sendSyncMessage(String topic, String tag, String message) {
        log.info("UserCouponProducer.sendSyncMessage, topic is {}, tag is {}, message is {}", topic, tag, message);
        rocketMqService.sendSyncMsg(topic, message, tag);
    }
    
    public void sendSyncMessage(String topic, String message) {
        log.info("UserCouponProducer.sendSyncMessage, topic is {}, message is {}", topic, message);
        rocketMqService.sendSyncMsg(topic, message);
    }
    
    public void sendSyncMessage(String message) {
        log.info("UserCouponProducer.sendSyncMessage, message is {}", message);
        rocketMqService.sendSyncMsg(MqProducerConstant.USER_COUPON_COMMON_TOPIC, message);
    }
}
