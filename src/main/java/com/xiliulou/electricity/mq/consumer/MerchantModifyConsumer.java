package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.MerchantModify;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 商户升级后重新计算返利差额
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-20-11:08
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.MERCHANT_MODIFY_TOPIC, consumerGroup = MqConsumerConstant.MERCHANT_MODIFY_CONSUMER_GROUP, consumeThreadMax = 3)
public class MerchantModifyConsumer implements RocketMQListener<String> {
    
    @Autowired
    private RebateConfigService rebateConfigService;
    
    @Override
    public void onMessage(String message) {
        log.info("MERCHANT MODIFY CONSUMER INFO!received msg={}", message);
        MerchantModify merchantModify = null;
        
        try {
            merchantModify = JsonUtil.fromJson(message, MerchantModify.class);
        } catch (Exception e) {
            log.error("MERCHANT MODIFY CONSUMER ERROR!parse fail,msg={}", message, e);
        }
        
        if (Objects.isNull(merchantModify) || Objects.isNull(merchantModify.getMerchantId())) {
            return;
        }
        
        
    }
}
