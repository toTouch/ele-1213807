package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.AuthPayStatusBO;
import com.xiliulou.electricity.dto.FreeDepositDelayDTO;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.query.FreeDepositAuthToPayStatusQuery;
import com.xiliulou.electricity.query.FreeDepositCancelAuthToPayQuery;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.FreeDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: AuthPayConsumer
 * @description: 代扣
 * @author: renhang
 * @create: 2024-08-26 11:07
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.FREE_DEPOSIT_TOPIC_NAME, selectorExpression = MqProducerConstant.AUTH_APY_TAG_NAME, consumerGroup = MqConsumerConstant.AUTH_PAY_CONSUMER_GROUP, consumeThreadMax = 3)
public class AuthPayConsumer implements RocketMQListener<String> {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Resource
    private FreeDepositService freeDepositService;
    
    @Override
    public void onMessage(String msg) {
        if (StrUtil.isBlank(msg)) {
            log.warn("AuthPayConsumer.accept.msg is null");
            return;
        }
        log.info("AuthPayConsumer Access Msg INFO! msg is {}", msg);
        
        FreeDepositDelayDTO dto = JsonUtil.fromJson(msg, FreeDepositDelayDTO.class);
        MDC.put("traceId", dto.getMdc());
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(dto.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("AuthPayConsumer WARN! freeDepositOrder is null, orderId is {}", dto.getOrderId());
            return;
        }
        
        FreeDepositAlipayHistory alipayHistory = freeDepositAlipayHistoryService.queryByAuthOrderId(dto.getAuthPayOrderId());
        if (Objects.isNull(alipayHistory)) {
            log.warn("AuthPayConsumer WARN! alipayHistory is null, orderId is {}", dto.getAuthPayOrderId());
            return;
        }
        // 如果不是交易处理中，返回
        if (!Objects.equals(alipayHistory.getPayStatus(), FreeDepositOrder.PAY_STATUS_DEALING)) {
            log.info("AuthPayConsumer Info! alipayHistory.payStatus not need update,payStatus is {}, orderId is {}", alipayHistory.getPayStatus(),
                    alipayHistory.getAuthPayOrderId());
            return;
        }
        
        // 再次查询代扣状态
        FreeDepositAuthToPayStatusQuery freeDepositAuthToPayStatusQuery = FreeDepositAuthToPayStatusQuery.builder().authPayOrderId(alipayHistory.getAuthPayOrderId())
                .authNo(freeDepositOrder.getAuthNo()).channel(dto.getChannel()).orderId(alipayHistory.getOrderId()).tenantId(freeDepositOrder.getTenantId())
                .uid(freeDepositOrder.getUid()).build();
        AuthPayStatusBO authPayStatusBO = freeDepositService.queryAuthToPayStatus(freeDepositAuthToPayStatusQuery);
        
        log.info("AuthPayConsumer Info! queryAuthPayStatus.result is {}", Objects.nonNull(authPayStatusBO) ? JsonUtil.toJson(authPayStatusBO) : "null");
        
        // 如果查询失败也修改为失败
        if (Objects.isNull(authPayStatusBO)) {
            log.warn("AuthPayConsumer Warn! queryAuthPayStatus.authPayStatusBO is null");
            updateOrderStatus(alipayHistory);
            return;
        }
        
        // 如果代扣中1和代扣失败2，更新为失败
        if (Objects.equals(authPayStatusBO.getOrderStatus(), FreeDepositOrder.PAY_STATUS_DEALING) || Objects.equals(authPayStatusBO.getOrderStatus(),
                FreeDepositOrder.PAY_STATUS_DEAL_FAIL)) {
            
            // 更新退款订单为失败
            updateOrderStatus(alipayHistory);
            
            // 执行取消代扣
            FreeDepositCancelAuthToPayQuery cancelAuthToPayQuery = FreeDepositCancelAuthToPayQuery.builder().orderId(alipayHistory.getOrderId())
                    .authPayOrderId(alipayHistory.getAuthPayOrderId()).channel(dto.getChannel()).tenantId(freeDepositOrder.getTenantId()).uid(freeDepositOrder.getUid()).build();
            freeDepositService.cancelAuthPay(cancelAuthToPayQuery);
        }
    }
    
    private void updateOrderStatus(FreeDepositAlipayHistory alipayHistory) {
        // 更新退款订单为失败
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setId(alipayHistory.getId());
        if (StrUtil.isEmpty(alipayHistory.getRemark())) {
            freeDepositAlipayHistory.setRemark("代扣超时关闭");
        }
        freeDepositAlipayHistory.setPayStatus(FreeDepositOrder.PAY_STATUS_DEAL_FAIL);
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistoryService.update(freeDepositAlipayHistory);
    }
    
}
