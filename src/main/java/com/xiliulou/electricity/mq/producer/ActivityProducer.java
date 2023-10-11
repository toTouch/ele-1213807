package com.xiliulou.electricity.mq.producer;

import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 活动的 Producer
 *
 * @author xiaohui.song
 **/
@Deprecated
@Slf4j
@Component
public class ActivityProducer {
    
    @Resource
    private RocketMqService rocketMqService;
    
    public void sendAsyncMessage(String topic, String tag, String message) {
        log.info("ActivityProducer.sendAsyncMessage, topic is {}, tag is {}, message is {}", topic, tag, message);
        rocketMqService.sendAsyncMsg(topic, message, tag);
    }
    
    public void sendAsyncMessage(String topic, String message) {
        log.info("ActivityProducer.sendAsyncMessage, topic is {}, message is {}", topic, message);
        rocketMqService.sendAsyncMsg(topic, message);
    }
    
    public void sendAsyncMessage(String message) {
        log.info("ActivityProducer.sendAsyncMessage, message is {}", message);
        rocketMqService.sendAsyncMsg(MqProducerConstant.ACTIVITY_COMMON_TOPIC, message);
    }
    
    public void sendSyncMessage(String topic, String tag, String message) {
        log.info("ActivityProducer.sendSyncMessage, topic is {}, tag is {}, message is {}", topic, tag, message);
        rocketMqService.sendSyncMsg(topic, message, tag);
    }
    
    public void sendSyncMessage(String topic, String message) {
        log.info("ActivityProducer.sendSyncMessage, topic is {}, message is {}", topic, message);
        rocketMqService.sendSyncMsg(topic, message);
    }
    
    public void sendSyncMessage(String message) {
        log.info("ActivityProducer.sendSyncMessage, message is {}", message);
        rocketMqService.sendSyncMsg(MqProducerConstant.ACTIVITY_COMMON_TOPIC, message);
    }
    
}
