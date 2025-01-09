package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.cabinet.ElectricityCabinetSendNormalDTO;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author maxiaodong
 * @date 2024/8/27 13:50
 * @desc 柜机normal结果检测
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.CABINET_NORMAL_RESULT_TOPIC_NAME, consumerGroup = MqConsumerConstant.CABINET_NORMAL_RESULT_GROUP, consumeThreadMax = 5)
public class CabinetNormalResultConsumer implements RocketMQListener<String> {
    @Resource
    private RedisService redisService;
    
    public void onMessage(String message) {
        log.info("CABINET NORMAL RESULT INFO! received msg={}", message);

        ElectricityCabinetSendNormalDTO electricityCabinetSendNormalDTO = null;
        
        try {
            electricityCabinetSendNormalDTO = JsonUtil.fromJson(message, ElectricityCabinetSendNormalDTO.class);

            boolean flag = false;

            // 下发指令相应成功修改回收记录为已锁仓
            String result = redisService.get(CacheConstant.ELE_OPERATOR_CACHE_KEY + electricityCabinetSendNormalDTO.getSessionId());
            if (!StringUtils.isEmpty(result)) {
                Map<String, Object> map = JsonUtil.fromJson(result, Map.class);
                String value = map.get("success").toString();
                if ("true".equalsIgnoreCase(value)) {
                    flag = true;
                }
            }

            if (!flag) {
                log.info("CABINET NORMAL RESULT INFO! result fail operatorId:{}, cabinetId:{}", electricityCabinetSendNormalDTO.getOperatorId(), electricityCabinetSendNormalDTO.getCabinetId());
            }
        } catch (Exception e) {
            log.error("CABINET NORMAL RESULT!msg={}", message, e);
        } finally {
            MDC.clear();
        }
    }
}
