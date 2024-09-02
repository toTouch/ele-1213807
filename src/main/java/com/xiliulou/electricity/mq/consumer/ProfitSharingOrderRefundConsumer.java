package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingTradeOrderConstant;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.ProfitSharingTradeOrderRefund;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeMixedOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
import com.xiliulou.pay.base.exception.ProfitSharingException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/8/27 13:50
 * @desc
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.PROFIT_SHARING_ORDER_REFUND_TOPIC, consumerGroup = MqConsumerConstant.PROFIT_SHARING_ORDER_REFUND_GROUP, consumeThreadMax = 5)
public class ProfitSharingOrderRefundConsumer implements RocketMQListener<String> {
    @Resource
    private ProfitSharingTradeOrderService profitSharingTradeOrderService;
    
    @Resource
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Resource
    private ProfitSharingOrderService profitSharingOrderService;
    
    @Resource
    private ProfitSharingTradeMixedOrderService profitSharingTradeMixedOrderService;
    
    
    public void onMessage(String message) {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
    
        log.info("PROFIT SHARING ORDE REFUND CONSUMER INFO!received msg={}", message);
        ProfitSharingTradeOrderRefund profitSharingTradeOrderRefund = null;
        
        try {
            profitSharingTradeOrderRefund = JsonUtil.fromJson(message, ProfitSharingTradeOrderRefund.class);
            
            // 校验参数
            if (!validateParams(profitSharingTradeOrderRefund)) {
                return;
            }
            
            // 校验业务订单和分账校验订单是否存在
            if (!checkOrder(profitSharingTradeOrderRefund)) {
                return;
            }
            
            // 更新分账订单
            updateProfitSharingOrder(profitSharingTradeOrderRefund);
            
        } catch (Exception e) {
            log.error("PROFIT SHARING ORDE REFUND CONSUMER ERROR!msg={}", message, e);
        } finally {
            MDC.clear();
        }
    }
    
    private void updateProfitSharingOrder(ProfitSharingTradeOrderRefund profitSharingTradeOrderRefund) {
        // 分账交易订单是否存在
        ProfitSharingTradeOrder profitSharingTradeOrder = profitSharingTradeOrderService.queryByOrderNo(profitSharingTradeOrderRefund.getOrderNo());
        if (Objects.isNull(profitSharingTradeOrder)) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!profit order is null, orderNo = {}, refundOrderNo = {}", profitSharingTradeOrderRefund.getOrderNo(), profitSharingTradeOrderRefund.getRefundOrderNo());
            return;
        }
        
        // 状态是否为待分账
        if (!Objects.equals(profitSharingTradeOrder.getProcessState(), ProfitSharingTradeOrderConstant.PROCESS_STATE_PENDING)) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!profit order status is not init, orderNo = {}, refundOrderNo = {}, processState = {}", profitSharingTradeOrderRefund.getOrderNo(), profitSharingTradeOrderRefund.getRefundOrderNo(), profitSharingTradeOrder.getProcessState());
            return;
        }
        
        // 业务类型为解冻的分账订单是否存在
        boolean existsUnfreezeByThirdOrderNo = profitSharingOrderService.existsUnfreezeByThirdOrderNo(profitSharingTradeOrder.getThirdOrderNo());
        if (existsUnfreezeByThirdOrderNo) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!exists unfreeze profit order, orderNo = {}, refundOrderNo = {}", profitSharingTradeOrderRefund.getOrderNo(), profitSharingTradeOrderRefund.getRefundOrderNo());
            return;
        }
    
        ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder = profitSharingTradeMixedOrderService.queryByThirdOrderNo(profitSharingTradeOrder.getThirdOrderNo());
        if (Objects.isNull(profitSharingTradeMixedOrder)) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!profit sharing trade mixed order is null, orderNo = {}, refundOrderNo = {}", profitSharingTradeOrderRefund.getOrderNo(), profitSharingTradeOrderRefund.getRefundOrderNo());
            return;
        }
        
        // 根据微信支付订单号查询分账交易订单中是否存在除了自己外不可退的订单
        boolean existsNotRefundByThirdOrderNo = profitSharingTradeOrderService.existsNotRefundByThirdOrderNo(profitSharingTradeOrder.getThirdOrderNo(), profitSharingTradeOrderRefund.getOrderNo());
        if (!existsNotRefundByThirdOrderNo) {
            try {
                profitSharingOrderService.doUnFreeze(profitSharingTradeMixedOrder);
            } catch (ProfitSharingException e) {
                log.error("PROFIT SHARING ORDER REFUND CONSUMER ERROR!", e);
            }
        }
        
        ProfitSharingTradeOrder profitSharingUpdate = new ProfitSharingTradeOrder();
        profitSharingUpdate.setId(profitSharingTradeOrder.getId());
        // 交易失败
        profitSharingUpdate.setProcessState(ProfitSharingTradeOrderConstant.PROCESS_STATE_INVALID);
       
        profitSharingUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 修改分账交易订单
        profitSharingTradeOrderService.updateById(profitSharingUpdate);
    }
    
    private boolean checkOrder(ProfitSharingTradeOrderRefund profitSharingTradeOrderRefund) {
        // 换电-套餐
        if (Objects.equals(profitSharingTradeOrderRefund.getOrderType(), ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode())) {
            BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectByRefundOrderNo(profitSharingTradeOrderRefund.getRefundOrderNo());
            if (Objects.isNull(batteryMembercardRefundOrder)) {
                log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!not found electricity member card refund order,orderNo={}", profitSharingTradeOrderRefund.getOrderNo());
                return false;
            }
            
            // 订单未成功
            if (!Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_SUCCESS)) {
                log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!electricity member card refund order status illegal,orderId={}", profitSharingTradeOrderRefund.getOrderNo());
                return false;
            }
        }
        
        return true;
    }
    
    private boolean validateParams(ProfitSharingTradeOrderRefund profitSharingTradeOrderRefund) {
        // 参数校验
        if (Objects.isNull(profitSharingTradeOrderRefund)) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!msg is null ");
            return false;
        }
        
        if (StringUtils.isEmpty(profitSharingTradeOrderRefund.getOrderNo())) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!orderNo is null ");
            return false;
        }
    
        if (Objects.isNull(profitSharingTradeOrderRefund.getRefundOrderNo())) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!refund orderNo is null, orderNo = {}", profitSharingTradeOrderRefund.getOrderNo());
            return false;
        }
        
        if (Objects.isNull(profitSharingTradeOrderRefund.getOrderType())) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!order type is null, orderNo = {}", profitSharingTradeOrderRefund.getOrderNo());
            return false;
        }
        
        // 校验业务类型是否存在
        if (!Objects.equals(profitSharingTradeOrderRefund.getOrderType(), ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode())) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!order type is not exist, orderNo = {}", profitSharingTradeOrderRefund.getOrderNo());
            return false;
        }
        
        return true;
    }
    
}
