package com.xiliulou.electricity.mq.producer;


import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.message.ThirdPartyMallDataDTO;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static com.xiliulou.electricity.mq.constant.MqProducerConstant.THIRD_PARTY_MALL_TOPIC;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 15:29:08
 */
@Slf4j
@Component
public class ThirdPartyMallProducer {
    
    private final RocketMqService rocketMqService;
    
    public ThirdPartyMallProducer(RocketMqService rocketMqService) {
        this.rocketMqService = rocketMqService;
    }
    
    public void sendMessage(ThirdPartyMallDataDTO message) {
        if (Objects.isNull(message)) {
            return;
        }
        try {
            String json = JsonUtil.toJson(message);
            Pair<Boolean, String> pair = rocketMqService.sendSyncMsg(THIRD_PARTY_MALL_TOPIC, json);
            if (!pair.getLeft()) {
                log.error("ThirdPartyMallProducer error! failed send message to the queue because: {}", Optional.ofNullable(pair.getRight()).orElse(""));
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("ThirdPartyMallProducer successfully sent message to the queue {}", json);
            }
        } catch (RuntimeException e) {
            log.error("ThirdPartyMallProducer error! error send message to the queue because: ", e);
        }
    }
}
