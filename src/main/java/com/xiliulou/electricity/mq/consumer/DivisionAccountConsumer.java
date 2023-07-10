package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 分账的 Consumer
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.DIVISION_ACCOUNT_COMMON_TOPIC, consumerGroup = MqConsumerConstant.DIVISION_ACCOUNT_COMMON_CONSUMER_GROUP)
public class DivisionAccountConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {

    }
}
