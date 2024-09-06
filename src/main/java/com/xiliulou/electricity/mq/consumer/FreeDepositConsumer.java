package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.FreeDepositDelayDTO;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: FreeDepositConsumer
 * @description:
 * @author: renhang
 * @create: 2024-08-26 11:07
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.FREE_DEPOSIT_TOPIC_NAME, selectorExpression = MqProducerConstant.FREE_DEPOSIT_TAG_NAME, consumerGroup = MqConsumerConstant.FREE_DEPOSIT_CONSUMER_GROUP)
public class FreeDepositConsumer implements RocketMQListener<String> {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Override
    public void onMessage(String msg) {
        if (StrUtil.isBlank(msg)) {
            log.warn("FreeDepositConsumer.accept.msg is null");
            return;
        }
        log.info("FreeDepositConsumer Access Msg INFO! msg is {} ", msg);
        
        FreeDepositDelayDTO dto = JsonUtil.fromJson(msg, FreeDepositDelayDTO.class);
        MDC.put("traceId", dto.getMdc());
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(dto.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("FreeDepositConsumer WARN! freeDepositOrder is null, orderId is {}", dto.getOrderId());
            return;
        }
        
        log.info("FreeDepositConsumer Access Msg INFO! freeDepositOrder.authStatus is {} , orderId is {}", freeDepositOrder.getAuthStatus(), dto.getOrderId());
        
        // 如果不是 冻结中的，不更新
        if (!(Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_PENDING_FREEZE) || Objects.equals(freeDepositOrder.getAuthStatus(),
                FreeDepositOrder.AUTH_FREEZING))) {
            log.info("FreeDepositConsumer INFO! freeDepositOrder.freeStatus is {}, orderId is {}", freeDepositOrder.getAuthStatus(), dto.getOrderId());
            return;
        }
        
        // 更新免押订单状态为最终态=失败
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_TIMEOUT);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
    }
    
}
