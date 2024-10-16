package com.xiliulou.electricity.event.publish;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.event.ThirdPartyMallEvent;
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
 * @date 2024/9/19 13:45:49
 */
@Slf4j
@Component
public class ThirdPartyMallPublish {
    
    private final RocketMqService rocketMqService;
    
    public ThirdPartyMallPublish(RocketMqService rocketMqService) {
        this.rocketMqService = rocketMqService;
    }
    
    public void publish(ThirdPartyMallEvent message) {
        if (Objects.isNull(message)) {
            log.warn("ThirdPartyMallProducer error! message is null");
            return;
        }
        
        if (Objects.isNull(message.getTenantId())) {
            log.warn("ThirdPartyMallProducer error! tenantId is null");
            return;
        }
        
        try {
            String json = JsonUtil.toJson(message.toDTO());
            int delayLevel = Objects.isNull(message.getDelayLevel()) ? 0 : message.getDelayLevel();
            log.info("ThirdPartyMallProducer send message={}, delayLevel={}", json, delayLevel);
            Pair<Boolean, String> pair = rocketMqService.sendSyncMsg(THIRD_PARTY_MALL_TOPIC, json, "", "", delayLevel);
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
