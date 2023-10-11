package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.DivisionAccountRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 分账的 Consumer
 *
 * @author xiaohui.song
 **/

@Deprecated
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.DIVISION_ACCOUNT_COMMON_TOPIC, consumerGroup = MqConsumerConstant.DIVISION_ACCOUNT_COMMON_CONSUMER_GROUP)
public class DivisionAccountConsumer implements RocketMQListener<String> {
    
    //XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("DIVISION_ACCOUNT_CONSUMER_POOL", 5, "division_account_thread");
    
    @Autowired
    DivisionAccountRecordService divisionAccountRecordService;
    
    @Override
    public void onMessage(String message) {
        try {
            DivisionAccountOrderDTO divisionAccountOrderDTO = JsonUtil.fromJson(message, DivisionAccountOrderDTO.class);
            if (Objects.isNull(divisionAccountOrderDTO)) {
                return;
            }
            
            if (DivisionAccountEnum.DA_TYPE_PURCHASE.getCode().equals(divisionAccountOrderDTO.getDivisionAccountType())) {
                //使用MQ自身线程池消费消息，无需再开一个线程去处理。
                //divisionAccountRecordService.handleDivisionAccountByPackage(divisionAccountOrderDTO);
                
            } else if (DivisionAccountEnum.DA_TYPE_REFUND.getCode().equals(divisionAccountOrderDTO.getDivisionAccountType())) {
                //divisionAccountRecordService.handleRefundDivisionAccountByPackage(divisionAccountOrderDTO);
            }
        } catch (Exception e) {
            log.error("Division account consumer error! msg = {}", message, e);
        }
        
    }
}
