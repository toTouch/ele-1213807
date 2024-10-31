package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.BatteryRecycleDelayDTO;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderProcessStateEnum;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.ProfitSharingTradeOrderUpdate;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeMixedOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
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
@RocketMQMessageListener(topic = MqProducerConstant.BATTERY_RECYCLE_TOPIC_NAME, consumerGroup = MqConsumerConstant.BATTERY_RECYCLE_GROUP, consumeThreadMax = 5)
public class BatteryRecycleConsumer implements RocketMQListener<String> {
    
    public void onMessage(String message) {
        log.info("BATTERY RECYCLE CONSUMER INFO!received msg={}", message);
        
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
    
        BatteryRecycleDelayDTO batteryRecycleDelayDTO = null;
        
        try {
            batteryRecycleDelayDTO = JsonUtil.fromJson(message, BatteryRecycleDelayDTO.class);
        } catch (Exception e) {
            log.error("PROFIT SHARING ORDE CONSUMER ERROR!msg={}", message, e);
        } finally {
            MDC.clear();
        }
    }
}
