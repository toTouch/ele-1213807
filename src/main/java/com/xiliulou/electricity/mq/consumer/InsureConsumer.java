package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 保险的 Consumer
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.INSURE_COMMON_TOPIC, consumerGroup = MqConsumerConstant.INSURE_COMMON_CONSUMER_GROUP)
public class InsureConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {

    }
}
