package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.FreeDepositDelayDTO;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeDepositServiceWayEnums;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.query.FreeDepositCancelAuthToPayQuery;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
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
@RocketMQMessageListener(topic = MqProducerConstant.FREE_DEPOSIT_TOPIC_NAME, selectorExpression = MqProducerConstant.AUTH_APY_TAG_NAME, consumerGroup = MqConsumerConstant.AUTH_PAY_CONSUMER_GROUP)
public class AuthPayConsumer implements RocketMQListener<String> {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Resource
    private ApplicationContext applicationContext;
    
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
        // 成功/失败 return
        if (Objects.equals(alipayHistory.getPayStatus(), FreeDepositOrder.PAY_STATUS_DEAL_SUCCESS) || Objects.equals(alipayHistory.getPayStatus(),
                FreeDepositOrder.PAY_STATUS_DEAL_FAIL)) {
            log.info("AuthPayConsumer.status not update! alipayHistory.payStatus is {}, orderId is {}", alipayHistory.getPayStatus(), alipayHistory.getAuthPayOrderId());
            return;
        }
        
        // 更新退款订单为失败
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setId(alipayHistory.getId());
        if (StrUtil.isEmpty(alipayHistory.getRemark())) {
            freeDepositAlipayHistory.setRemark("代扣超时关闭");
        }
        freeDepositAlipayHistory.setPayStatus(FreeDepositOrder.PAY_STATUS_DEAL_FAIL);
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistoryService.update(freeDepositAlipayHistory);
        
        // 拍小组即使金额不足也代扣成功，所以如果5分钟没有代扣成功，执行取消代扣
        FreeDepositCancelAuthToPayQuery cancelAuthToPayQuery = FreeDepositCancelAuthToPayQuery.builder().orderId(alipayHistory.getOrderId())
                .authPayOrderId(alipayHistory.getAuthPayOrderId()).tenantId(freeDepositAlipayHistory.getTenantId()).uid(freeDepositOrder.getUid()).build();
        applicationContext.getBean(FreeDepositServiceWayEnums.PXZ.getImplService(), BaseFreeDepositService.class).cancelAuthPay(cancelAuthToPayQuery);
    }
    
}
