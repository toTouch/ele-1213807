package com.xiliulou.electricity.mq.producer;

import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 分账的 Producer
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
public class DivisionAccountProducer {

    @Resource
    private RocketMqService rocketMqService;


    public void sendAsyncMessage(String topic, String tag, String message) {

    }

    public void sendAsyncMessage(String topic, String message) {

    }

    public void sendAsyncMessage(String message) {

    }

    public void sendSyncMessage(String topic, String tag, String message) {

    }

    public void sendSyncMessage(String topic, String message) {

    }

    public void sendSyncMessage(String message) {

    }
}
