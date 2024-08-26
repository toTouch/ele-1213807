package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.FyFreeDepositDelayDTO;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: FyFreeDepositConsumer
 * @description:
 * @author: renhang
 * @create: 2024-08-26 11:07
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.FY_FREE_DEPOSIT_TOPIC_NAME, selectorExpression = MqProducerConstant.FY_FREE_DEPOSIT_TAG_NAME, consumerGroup = MqConsumerConstant.FY_FREE_DEPOSIT_CONSUMER_GROUP)
public class FyFreeDepositConsumer implements RocketMQListener<String> {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Override
    public void onMessage(String msg) {
        if (StrUtil.isBlank(msg)) {
            return;
        }
        
        FyFreeDepositDelayDTO dto = JsonUtil.fromJson(msg, FyFreeDepositDelayDTO.class);
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(dto.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("FyFreeDepositConsumer WARN! freeDepositOrder is null, orderId is{}", dto.getOrderId());
            return;
        }
        
        if (!Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_PENDING_FREEZE)) {
            log.info("FyFreeDepositConsumer INFO! freeDepositOrder.authStatus not is AUTH_PENDING_FREEZE, orderId is{}", dto.getOrderId());
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
