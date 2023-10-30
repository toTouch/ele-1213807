package com.xiliulou.electricity.mq.producer;

import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 保险的 Producer
 *
 * @author xiaohui.song
 **/
@Deprecated
@Slf4j
@Component
public class InsureProducer {
    
    @Resource
    private RocketMqService rocketMqService;
    
    
    public void sendAsyncMessage(String topic, String tag, String message) {
        rocketMqService.sendAsyncMsg(topic, message, tag);
    }
    
    public void sendAsyncMessage(String topic, String message) {
        rocketMqService.sendAsyncMsg(topic, message);
    }
    
    public void sendAsyncMessage(String message) {
        rocketMqService.sendAsyncMsg(MqProducerConstant.INSURE_COMMON_TOPIC, message);
    }
    
    public void sendSyncMessage(String topic, String tag, String message) {
        rocketMqService.sendSyncMsg(topic, message, tag);
    }
    
    public void sendSyncMessage(String topic, String message) {
        rocketMqService.sendSyncMsg(topic, message);
    }
    
    public void sendSyncMessage(String message) {
        rocketMqService.sendSyncMsg(MqProducerConstant.INSURE_COMMON_TOPIC, message);
    }
    
}
