package com.xiliulou.electricity.mq.producer;

import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/9 9:56
 */
@Component
@Slf4j
public class InstallmentProducer {
    
    @Resource
    private RocketMqService rocketMqService;
}
