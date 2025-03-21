package com.xiliulou.electricity.event.publish;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.event.ThirdPartyEvent;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 13:45:49
 */
@Slf4j
@Component
public class ThirdPartyPublish {
    
    private final RocketMqService rocketMqService;
    
    public ThirdPartyPublish(RocketMqService rocketMqService) {
        this.rocketMqService = rocketMqService;
    }
    
    public void publish(ThirdPartyEvent message) {
        if (Objects.isNull(message)) {
            log.warn("ThirdPartyPublish error! message is null");
            return;
        }
        
        if (Objects.isNull(message.getTenantId())) {
            log.warn("ThirdPartyPublish error! tenantId is null");
            return;
        }
        
        try {
            String json = JsonUtil.toJson(message.toDTO());
            int delayLevel = Objects.isNull(message.getDelayLevel()) ? 0 : message.getDelayLevel();
            log.info("ThirdPartyPublish send message={}, delayLevel={}", json, delayLevel);
            Pair<Boolean, String> pair = rocketMqService.sendSyncMsg(MqProducerConstant.THIRD_PARTY_SAAS_TOPIC, json, "", "", delayLevel);
            if (!pair.getLeft()) {
                log.error("ThirdPartyPublish error! failed send message to the queue because: {}", Optional.ofNullable(pair.getRight()).orElse(""));
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("ThirdPartyPublish successfully sent message to the queue {}", json);
            }
        } catch (RuntimeException e) {
            log.error("ThirdPartyPublish error! error send message to the queue because: ", e);
        }
    }
}
