package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.DivisionAccountRecord;
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
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.DIVISION_ACCOUNT_COMMON_TOPIC, consumerGroup = MqConsumerConstant.DIVISION_ACCOUNT_COMMON_CONSUMER_GROUP)
public class DivisionAccountConsumer implements RocketMQListener<String> {

    XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("DIVISION_ACCOUNT_CONSUMER_POOL", 5, "division_account_thread");

    @Autowired
    DivisionAccountRecordService divisionAccountRecordService;

    @Override
    public void onMessage(String message) {
        try {
            DivisionAccountOrderDTO divisionAccountOrderDTO = JsonUtil.fromJson(message, DivisionAccountOrderDTO.class);
            if (Objects.isNull(divisionAccountOrderDTO)) {
                return;
            }

            if(DivisionAccountRecord.TYPE_PURCHASE.equals(divisionAccountOrderDTO.getDivisionAccountType())){
                executorService.execute(() -> {
                    divisionAccountRecordService.handleDivisionAccountByPackage(divisionAccountOrderDTO.getOrderNo(), divisionAccountOrderDTO.getType());
                });
            }else if(DivisionAccountRecord.TYPE_REFUND.equals(divisionAccountOrderDTO.getDivisionAccountType())){
                executorService.execute(() -> {
                    divisionAccountRecordService.handleRefundDivisionAccountByPackage(divisionAccountOrderDTO.getOrderNo(), divisionAccountOrderDTO.getType());
                });
            }
        } catch (Exception e){
            log.error("Division account consumer error! msg = {}", message, e);
        }

    }
}
