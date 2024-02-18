package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.MerchantUpgrade;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 首次购买套餐
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-18-14:15
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.MERCHANT_UPGRADE_TOPIC, consumerGroup = MqConsumerConstant.MERCHANT_UPGRADE_CONSUMER_GROUP, consumeThreadMax = 3)
public class MerchantUpgradeConsumer implements RocketMQListener<String> {
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    @Autowired
    private UserInfoExtraService userInfoExtraService;
    
    @Override
    public void onMessage(String message) {
        log.info("MERCHANT UPGRADE CONSUMER INFO!received msg={}", message);
        MerchantUpgrade merchantUpgrade = null;
    
        try {
            merchantUpgrade = JsonUtil.fromJson(message, MerchantUpgrade.class);
        } catch (Exception e) {
            log.error("MERCHANT UPGRADE CONSUMER ERROR!parse fail,msg={}", message, e);
        }
        
        if(Objects.isNull(merchantUpgrade) || Objects.isNull(merchantUpgrade.getUid())){
            return;
        }
    
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(merchantUpgrade.getUid());
        if(Objects.isNull(userInfoExtra) || Objects.isNull(userInfoExtra.getMerchantId())){
           return;
        }
    
        //TODO 查询商户   商户禁用
        //        if(Objects.equals( , )){
        //            log.warn("MERCHANT UPGRADE CONSUMER WARN!merchantAttr is disable,merchantId={}", userInfoExtra.getMerchantId());
        //            return;
        //        }
    
        MerchantAttr merchantAttr = merchantAttrService.queryByMerchantId(userInfoExtra.getMerchantId());
        if(Objects.isNull(merchantAttr)){
            log.warn("MERCHANT UPGRADE CONSUMER WARN!merchantAttr is null,merchantId={}", userInfoExtra.getMerchantId());
            return;
        }
        

    
        //查询用户邀请记录
        
        //更新邀请记录
    
    
    
    }
}
