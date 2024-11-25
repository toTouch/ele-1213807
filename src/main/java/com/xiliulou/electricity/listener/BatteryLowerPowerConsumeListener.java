package com.xiliulou.electricity.listener;

import cn.hutool.core.util.RandomUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.dto.BatteryPowerNotifyDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.electricity.service.template.MiniTemplateMsgBizService;
import com.xiliulou.electricity.service.template.TemplateConfigService;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2023/5/9 19:14
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = MqProducerConstant.TOPIC_BATTERY_POWER, consumerGroup = MqConsumerConstant.BATTERY_CONSUMER, consumeThreadMax = 2)
public class BatteryLowerPowerConsumeListener implements RocketMQListener<String> {
    
    XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("LOWER_POWER_CONSUMER_POOL", 4, "lower_power_thread");
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    PayConfigBizService payConfigBizService;
    
    @Autowired
    TemplateConfigService templateConfigService;
    
    @Autowired
    WeChatAppTemplateService weChatAppTemplateService;
    
    
    @Resource
    private MiniTemplateMsgBizService miniTemplateMsgBizService;
    
    @Override
    public void onMessage(String message) {
        TtlTraceIdSupport.set();
        try {
            log.info("Battery Lower message:{}", message);
            BatteryPowerNotifyDto batteryPowerNotifyDto = JsonUtil.fromJson(message, BatteryPowerNotifyDto.class);
            if (Objects.isNull(batteryPowerNotifyDto)) {
                return;
            }
            
            executorService.execute(() -> {
                handleSendWxTemplate(batteryPowerNotifyDto);
            });
            
            
        } catch (Exception e) {
            log.error("CONSUMER ERROR! msg={}", message, e);
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
    
    private void handleSendWxTemplate(BatteryPowerNotifyDto batteryPowerNotifyDto) {
        ElectricityBattery electricityBattery = electricityBatteryService.queryUserAttrBySnFromDb(batteryPowerNotifyDto.getSn());
        if (Objects.isNull(electricityBattery)) {
            log.warn("BATTERY NOT FOUND! Can't send template! sn={}", batteryPowerNotifyDto.getSn());
            return;
        }
        
        Long uid = electricityBattery.getUid();
        if (Objects.isNull(uid)) {
            return;
        }
        miniTemplateMsgBizService
                .sendLowBatteryReminder(electricityBattery.getTenantId(), electricityBattery.getUid(), batteryPowerNotifyDto.getSoc() + "%", batteryPowerNotifyDto.getSn());
    }
}
