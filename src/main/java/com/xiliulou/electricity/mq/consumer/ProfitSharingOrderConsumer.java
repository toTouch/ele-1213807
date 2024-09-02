package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingTradeOrderConstant;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.ProfitSharingTradeOrderUpdate;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
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
@RocketMQMessageListener(topic = MqProducerConstant.PROFIT_SHARING_ORDER_TOPIC, consumerGroup = MqConsumerConstant.PROFIT_SHARING_ORDER_GROUP, consumeThreadMax = 5)
public class ProfitSharingOrderConsumer implements RocketMQListener<String> {
    @Resource
    private ProfitSharingTradeOrderService profitSharingTradeOrderService;
    
    @Resource
    private EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private InsuranceOrderService insuranceOrderService;
    
    public void onMessage(String message) {
        log.info("PROFIT SHARING ORDE CONSUMER INFO!received msg={}", message);
        ProfitSharingTradeOrderUpdate profitSharingTradeOrderUpdate = null;
        
        try {
            profitSharingTradeOrderUpdate = JsonUtil.fromJson(message, ProfitSharingTradeOrderUpdate.class);
            
            // 校验参数
            if (!validateParams(profitSharingTradeOrderUpdate)) {
                return;
            }
            
            // 校验业务订单和分账校验订单是否存在
            if (!checkOrder(profitSharingTradeOrderUpdate)) {
                return;
            }
            
            // 更新分账订单
            updateProfitSharingOrder(profitSharingTradeOrderUpdate);
            
        } catch (Exception e) {
            log.error("PROFIT SHARING ORDE CONSUMER ERROR!msg={}", message, e);
        }
    }
    
    private void updateProfitSharingOrder(ProfitSharingTradeOrderUpdate profitSharingTradeOrderUpdate) {
        ProfitSharingTradeOrder profitSharingTradeOrder = profitSharingTradeOrderService.queryByOrderNo(profitSharingTradeOrderUpdate.getOrderNo());
        if (Objects.isNull(profitSharingTradeOrder)) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!profit order is null, orderNo = {}", profitSharingTradeOrderUpdate.getOrderNo());
            return;
        }
    
        if (!Objects.equals(profitSharingTradeOrder.getProcessState(), ProfitSharingTradeOrderConstant.PROCESS_STATE_INIT)) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!profit order status is not init, orderNo = {}, processState = {}", profitSharingTradeOrderUpdate.getOrderNo(), profitSharingTradeOrder.getProcessState());
            return;
        }
    
        ProfitSharingTradeOrder profitSharingUpdate = new ProfitSharingTradeOrder();
        profitSharingUpdate.setId(profitSharingTradeOrder.getId());
        profitSharingUpdate.setThirdOrderNo(profitSharingTradeOrderUpdate.getThirdOrderNo());
        // 待发起分账
        profitSharingUpdate.setProcessState(ProfitSharingTradeOrderConstant.PROCESS_STATE_PENDING);
        profitSharingUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 修改分账交易订单
        profitSharingTradeOrderService.updateById(profitSharingUpdate);
    }
    
    private boolean checkOrder(ProfitSharingTradeOrderUpdate profitSharingTradeOrderUpdate) {
        // 根据业务类型判断对应的业务订单是否存在
        
        // 服务费
        if (Objects.equals(profitSharingTradeOrderUpdate.getOrderType(), ProfitSharingBusinessTypeEnum.BATTERY_SERVICE_FEE.getCode())) {
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(profitSharingTradeOrderUpdate.getOrderNo());
            if (Objects.isNull(eleBatteryServiceFeeOrder)) {
                log.warn("PROFIT SHARING ORDE CONSUMER WARN!not found battery service fee order,orderId={}", profitSharingTradeOrderUpdate.getOrderNo());
                return false;
            }
            
            // 服务费订单未成功
            if (!Objects.equals(eleBatteryServiceFeeOrder.getStatus(), EleBatteryServiceFeeOrder.STATUS_SUCCESS)) {
                log.warn("PROFIT SHARING ORDE CONSUMER WARN!battery service fee order status illegal,orderId={}", profitSharingTradeOrderUpdate.getOrderNo());
                return false;
            }
        }
        
        // 换电-套餐
        if (Objects.equals(profitSharingTradeOrderUpdate.getOrderType(), ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode())) {
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(profitSharingTradeOrderUpdate.getOrderNo());
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("PROFIT SHARING ORDE CONSUMER WARN!not found electricity member card order,orderNo={}", profitSharingTradeOrderUpdate.getOrderNo());
                return false;
            }
            
            // 订单未成功
            if (!Objects.equals(electricityMemberCardOrder.getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS)) {
                log.warn("PROFIT SHARING ORDE CONSUMER WARN!electricity member card order status illegal,orderId={}, electricityMemberCardOrder = {}", profitSharingTradeOrderUpdate.getOrderNo(), electricityMemberCardOrder);
                return false;
            }
        }
        
        // 换电-保险
        if (Objects.equals(profitSharingTradeOrderUpdate.getOrderType(), ProfitSharingBusinessTypeEnum.INSURANCE.getCode())) {
            // 保险订单
            InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(profitSharingTradeOrderUpdate.getOrderNo());
            if (Objects.isNull(insuranceOrder)) {
                log.warn("PROFIT SHARING ORDE CONSUMER WARN!not found insurance order orderId={}", profitSharingTradeOrderUpdate.getOrderNo());
                return false;
            }
    
            // 订单未成功
            if (!Objects.equals(insuranceOrder.getStatus(), InsuranceOrder.STATUS_SUCCESS)) {
                log.warn("PROFIT SHARING ORDE CONSUMER WARN!insurance order order status illegal,orderId={}, insuranceOrder = {}", profitSharingTradeOrderUpdate.getOrderNo(), insuranceOrder);
                return false;
            }
        }
        
        return true;
    }
    
    private boolean validateParams(ProfitSharingTradeOrderUpdate profitSharingTradeOrderUpdate) {
        // 参数校验
        if (Objects.isNull(profitSharingTradeOrderUpdate)) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!msg is null ");
            return false;
        }
        
        if (StringUtils.isEmpty(profitSharingTradeOrderUpdate.getOrderNo())) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!order is null ");
            return false;
        }
        
        if (Objects.isNull(profitSharingTradeOrderUpdate.getOrderType())) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!order type is null, orderNo = {}", profitSharingTradeOrderUpdate.getOrderNo());
            return false;
        }
        
        // 校验业务类型是否存在
        if (!(Objects.equals(profitSharingTradeOrderUpdate.getOrderType(), ProfitSharingBusinessTypeEnum.BATTERY_SERVICE_FEE.getCode())
                        || Objects.equals(profitSharingTradeOrderUpdate.getOrderType(), ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode()) || Objects.equals(profitSharingTradeOrderUpdate.getOrderType(), ProfitSharingBusinessTypeEnum.INSURANCE.getCode())
                )) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!order type is not exist, orderNo = {}", profitSharingTradeOrderUpdate.getOrderNo());
            return false;
        }
        
        if (Objects.isNull(profitSharingTradeOrderUpdate.getTradeStatus())) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!order status is null, orderNo = {}", profitSharingTradeOrderUpdate.getOrderNo());
            return false;
        }
        
        // 校验状态是否成功
        if (!Objects.equals(profitSharingTradeOrderUpdate.getTradeStatus(), ElectricityTradeOrder.STATUS_SUCCESS)) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!trade status is not success, orderNo = {}, tradeStatus = {}", profitSharingTradeOrderUpdate.getOrderNo(), profitSharingTradeOrderUpdate.getTradeStatus());
            return false;
        }
        
        if (StringUtils.isEmpty(profitSharingTradeOrderUpdate.getThirdOrderNo())) {
            log.warn("PROFIT SHARING ORDE CONSUMER WARN!third orderNo is null, orderNo = {}", profitSharingTradeOrderUpdate.getOrderNo());
            return false;
        }
    
        return true;
    }
    
}
