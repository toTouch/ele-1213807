package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.FyFreeDepositDelayDTO;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: FyAuthPayConsumer
 * @description: 代扣
 * @author: renhang
 * @create: 2024-08-26 11:07
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.FY_FREE_DEPOSIT_TOPIC_NAME, selectorExpression = MqProducerConstant.FY_AUTH_APY_TAG_NAME, consumerGroup = MqConsumerConstant.FY_FREE_DEPOSIT_CONSUMER_GROUP)
public class FyAuthPayConsumer implements RocketMQListener<String> {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    
    @Override
    public void onMessage(String msg) {
        if (StrUtil.isBlank(msg)) {
            return;
        }
        
        FyFreeDepositDelayDTO dto = JsonUtil.fromJson(msg, FyFreeDepositDelayDTO.class);
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(dto.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("FyUnFreeDepositConsumer WARN! freeDepositOrder is null, orderId is{}", dto.getOrderId());
            return;
        }
        
        if (!Objects.equals(freeDepositOrder.getPayStatus(), FreeDepositOrder.PAY_STATUS_DEALING)) {
            log.info("FyUnFreeDepositConsumer INFO! freeDepositOrder.authStatus not is PAY_STATUS_DEALING, orderId is{}", dto.getOrderId());
            return;
        }
        
        // 更新冻结状态为失败
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(FreeDepositOrder.PAY_STATUS_DEAL_CLOSE);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        // 更新退款订单为失败
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setOrderId(freeDepositOrder.getOrderId());
        freeDepositAlipayHistory.setPayStatus(FreeDepositOrder.PAY_STATUS_DEAL_CLOSE);
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistoryService.updateByOrderId(freeDepositAlipayHistory);
    }
    
}
