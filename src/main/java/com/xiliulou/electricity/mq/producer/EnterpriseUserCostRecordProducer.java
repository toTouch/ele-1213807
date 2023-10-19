package com.xiliulou.electricity.mq.producer;

import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 10:53
 */

@Slf4j
@Component
public class EnterpriseUserCostRecordProducer {
    
    @Resource
    private RocketMqService rocketMqService;
    
    
    public void sendAsyncMessage(String topic, String tag, String message) {
        log.info("EnterpriseUserCostRecordProducer.sendAsyncMessage, topic is {}, tag is {}, message is {}", topic, tag, message);
        rocketMqService.sendAsyncMsg(topic, message, tag);
    }
    
    public void sendAsyncMessage(String topic, String message) {
        log.info("EnterpriseUserCostRecordProducer.sendAsyncMessage, topic is {}, message is {}", topic, message);
        rocketMqService.sendAsyncMsg(topic, message);
    }
    
    public void sendAsyncMessage(String message) {
        log.info("EnterpriseUserCostRecordProducer.sendAsyncMessage, message is {}", message);
        rocketMqService.sendAsyncMsg(MqProducerConstant.ENTERPRISE_USER_COST_RECORD_TOPIC, message);
    }
    
    public void sendSyncMessage(String topic, String tag, String message) {
        log.info("EnterpriseUserCostRecordProducer.sendSyncMessage, topic is {}, tag is {}, message is {}", topic, tag, message);
        rocketMqService.sendSyncMsg(topic, message, tag);
    }
    
    public void sendSyncMessage(String topic, String message) {
        log.info("EnterpriseUserCostRecordProducer.sendSyncMessage, topic is {}, message is {}", topic, message);
        rocketMqService.sendSyncMsg(topic, message);
    }
    
    public void sendSyncMessage(String message) {
        log.info("EnterpriseUserCostRecordProducer.sendSyncMessage, message is {}", message);
        rocketMqService.sendSyncMsg(MqProducerConstant.ENTERPRISE_USER_COST_RECORD_TOPIC, message);
    }
    
}
