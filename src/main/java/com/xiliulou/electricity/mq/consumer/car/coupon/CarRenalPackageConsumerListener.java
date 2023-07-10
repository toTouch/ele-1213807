package com.xiliulou.electricity.mq.consumer.car.coupon;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xiliulou.electricity.mq.data.MqDataMessageModel;

import lombok.extern.slf4j.Slf4j;

/**
 * 租车套餐的消费者监听
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = "", consumerGroup = "")
public class CarRenalPackageConsumerListener implements RocketMQListener<MqDataMessageModel> {

    @Override
    public void onMessage(MqDataMessageModel mqDataMessageModel) {
        log.info("CarRenalPackageConsumerListener 收到消息, msg is {}", JSON.toJSONString(mqDataMessageModel));
    }
}
