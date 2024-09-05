package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.FreeDepositDelayDTO;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: UnFreeDepositConsumer
 * @description: 解冻
 * @author: renhang
 * @create: 2024-08-26 11:07
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.FREE_DEPOSIT_TOPIC_NAME, selectorExpression = MqProducerConstant.UN_FREE_DEPOSIT_TAG_NAME, consumerGroup = MqConsumerConstant.UN_FREE_DEPOSIT_CONSUMER_GROUP)
public class UnFreeDepositConsumer implements RocketMQListener<String> {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private EleRefundOrderService eleRefundOrderService;
    
    @Override
    public void onMessage(String msg) {
        if (StrUtil.isBlank(msg)) {
            log.warn("UnFreeDepositConsumer.accept.msg is null");
            return;
        }
        log.info("UnFreeDepositConsumer Access Msg INFO! msg is {} ", msg);
        
        FreeDepositDelayDTO dto = JsonUtil.fromJson(msg, FreeDepositDelayDTO.class);
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(dto.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("UnFreeDepositConsumer WARN! freeDepositOrder is null, orderId is {}", dto.getOrderId());
            return;
        }
        
        log.info("UnFreeDepositConsumer Access Msg INFO! orderId is {}, authStatus is {}", dto.getOrderId(), freeDepositOrder.getAuthStatus());
        
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN) || Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_TIMEOUT)) {
            log.info("UnFreeDepositConsumer.status not update! freeDepositOrder.authStatus is {}, orderId is {}", freeDepositOrder.getAuthStatus(), dto.getOrderId());
            return;
        }
        
        // 更新冻结状态为失败
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_TIMEOUT);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        // 更新退款订单为失败
        EleRefundOrder eleRefundOrder = eleRefundOrderService.selectLatestRefundDepositOrder(freeDepositOrder.getOrderId());
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_FAIL);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);
    }
    
}
