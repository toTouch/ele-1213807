package com.xiliulou.electricity.mq.producer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.BatteryRecycleDelayDTO;
import com.xiliulou.electricity.dto.FreeDepositDelayDTO;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/10/31 11:47
 * @desc
 */
@Slf4j
@Component
public class BatteryRecycleProducer {
    @Resource
    private RocketMqService rocketMqService;
    
    /**
     * 默认延迟30s
     *
     * @param dto
     */
    public void sendDelayMessage(BatteryRecycleDelayDTO dto) {
        if (Objects.isNull(dto)) {
            log.warn("Battery recycle Delay Warn! dto is null");
            return;
        }
        
        log.info("BATTERY RECYCLE LOCK CELL INFO! msg:{}", dto);
        
        rocketMqService.sendSyncMsg(MqProducerConstant.BATTERY_RECYCLE_TOPIC_NAME, JsonUtil.toJson(dto), "", "", 4);
    }
}
