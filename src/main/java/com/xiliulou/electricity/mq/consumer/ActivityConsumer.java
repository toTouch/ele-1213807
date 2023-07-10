package com.xiliulou.electricity.mq.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * 活动的 Consumer
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.ACTIVITY_COMMON_TOPIC, consumerGroup = MqConsumerConstant.ACTIVITY_COMMON_CONSUMER_GROUP)
public class ActivityConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {

    }
}
