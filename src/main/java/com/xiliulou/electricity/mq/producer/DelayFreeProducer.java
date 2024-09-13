package com.xiliulou.electricity.mq.producer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.FreeDepositDelayDTO;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: DelayFreeProducer
 * @description:
 * @author: renhang
 * @create: 2024-08-28 11:45
 */
@Slf4j
@Component
public class DelayFreeProducer {
    
    @Resource
    private RocketMqService rocketMqService;
    
    public static final String ORDER_DATE_FORMAT = "yyyyMMddHHmmss";
    
    
    /**
     * 默认延迟5分钟
     *
     * @param dto
     * @param tag
     */
    public void sendDelayFreeMessage(FreeDepositDelayDTO dto, String tag) {
        if (Objects.isNull(dto)) {
            log.warn("Free Delay Warn! dto is null");
            return;
        }
        log.info("Free Delay Info! sendDelayFreeMessage.dto is {}, tag is {}", JsonUtil.toJson(dto), tag);
        String key = "free" + DateUtil.format(DateUtil.date(), ORDER_DATE_FORMAT) + RandomUtil.randomInt(1000, 9999);
        rocketMqService.sendSyncMsg(MqProducerConstant.FREE_DEPOSIT_TOPIC_NAME, JsonUtil.toJson(dto), tag, key, 9);
    }
    
    
}
