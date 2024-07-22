package com.xiliulou.electricity.mq.producer;


import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.message.MessageDTO;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static com.xiliulou.electricity.mq.constant.MqProducerConstant.AUX_MQ_TOPIC_NAME;
import static com.xiliulou.electricity.mq.constant.MqProducerConstant.MQ_TOPIC_SITE_MESSAGE_TAG_NAME;

/**
 * <p>
 * Description: This class is OperationRecordProducer!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/25
 **/
@Slf4j
@Component
public class SiteMessageProducer {
    
    private final RocketMqService rocketMqService;
    
    public SiteMessageProducer(RocketMqService rocketMqService) {
        this.rocketMqService = rocketMqService;
    }
    
    public void sendMessage(MessageDTO message) {
        if (Objects.isNull(message)) {
            return;
        }
        try {
            String json = JsonUtil.toJson(message);
            Pair<Boolean, String> pair = rocketMqService.sendSyncMsg(AUX_MQ_TOPIC_NAME, json, MQ_TOPIC_SITE_MESSAGE_TAG_NAME);
            if (!pair.getLeft()) {
                log.error("Failed send message to the queue because: {}", Optional.ofNullable(pair.getRight()).orElse(""));
                return;
            }
            if (log.isDebugEnabled()){
                log.debug("Successfully sent user operation records to the queue {}", json);
            }
        } catch (RuntimeException e) {
            log.error("Error send message to the queue because: ", e);
        }
    }
}
