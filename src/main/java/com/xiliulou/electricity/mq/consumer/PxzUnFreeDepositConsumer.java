package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.PxzUnFreeDepositDTO;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: PxzUnFreeDepositConsumer
 * @description:
 * @author: renhang
 * @create: 2024-08-25 11:25
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.PXZ_UN_FREE_DEPOSIT_TOPIC_NAME, consumerGroup = MqConsumerConstant.PXZ_UN_FREE_DEPOSIT_CONSUMER_GROUP)
public class PxzUnFreeDepositConsumer implements RocketMQListener<String> {
    
    @Resource
    RestTemplateService restTemplateService;
    
    public static final String url = "/outer/unFree/notified/1";
    
    @Override
    public void onMessage(String message) {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        
        log.info("PxzUnFreeDepositConsumer Info! received msg is {}", message);
        if (StrUtil.isEmpty(message)) {
            log.error("PxzUnFreeDepositConsumer error! received msg is empty");
            return;
        }
        
        PxzUnFreeDepositDTO dto = JsonUtil.fromJson(message, PxzUnFreeDepositDTO.class);
        
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", dto.getOrderId());
        map.put("authStatus", dto.getAuthStatus());
        restTemplateService.postForString(url, map);
    }
}
