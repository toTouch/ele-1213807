package com.xiliulou.electricity.listener;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.electricity.dto.BatteryPowerNotifyDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TemplateConfigService;
import com.xiliulou.electricity.service.UserOauthBindService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2023/5/9 19:14
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = MqProducerConstant.TOPIC_BATTERY_POWER, consumerGroup = MqConsumerConstant.BATTERY_CONSUMER)
public class BatteryLowerPowerConsumeListener implements RocketMQListener<String> {
    XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("LOWER_POWER_CONSUMER_POOL", 4, "lower_power_thread");
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Autowired
    UserOauthBindService userOauthBindService;

    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    @Autowired
    TemplateConfigService templateConfigService;

    @Autowired
    WeChatAppTemplateService weChatAppTemplateService;

    @Override
    public void onMessage(String message) {
        try {
            BatteryPowerNotifyDto batteryPowerNotifyDto = JsonUtil.fromJson(message, BatteryPowerNotifyDto.class);
            if (Objects.isNull(batteryPowerNotifyDto)) {
                return;
            }

            executorService.execute(() -> {
                handleSendWxTemplate(batteryPowerNotifyDto);
            });


        } catch (Exception e) {
            log.error("CONSUMER ERROR! msg={}", message, e);
        }
    }

    private void handleSendWxTemplate(BatteryPowerNotifyDto batteryPowerNotifyDto) {
        ElectricityBattery electricityBattery = electricityBatteryService.queryUserAttrBySnFromDb(batteryPowerNotifyDto.getSn());
        if (Objects.isNull(electricityBattery)) {
            log.error("BATTERY NOT FOUND! Can't send template! sn={}", batteryPowerNotifyDto.getSn());
            return;
        }

        Long uid = electricityBattery.getUid();
        if(Objects.isNull(uid)) {
            return;
        }

        Integer tenantId = electricityBattery.getTenantId();

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(electricityBattery.getUid(), electricityBattery.getTenantId());

        if (Objects.isNull(userOauthBind)) {
            log.error("USER_OAUTH_BIND IS NULL uid={},tenantId={},sn={}", uid, tenantId, batteryPowerNotifyDto.getSn());
            return;
        }
        String openId = userOauthBind.getThirdId();

        BaseMapper<ElectricityPayParams> mapper = electricityPayParamsService.getBaseMapper();
        QueryWrapper<ElectricityPayParams> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId);
        ElectricityPayParams ele = mapper.selectOne(wrapper);
        if (Objects.isNull(ele)) {
            log.error("ELECTRICITY_PAY_PARAMS IS NULL ERROR! tenantId={},sn={}", tenantId, batteryPowerNotifyDto.getSn());
            return;
        }

        TemplateConfigEntity templateConfigEntity = templateConfigService.queryByTenantIdFromCache(tenantId);

        if (Objects.isNull(templateConfigEntity) || Objects.isNull(
                templateConfigEntity.getBatteryOuttimeTemplate())) {
            log.warn("TEMPLATE_CONFIG IS NULL WARN! tenantId={},sn={}", tenantId, batteryPowerNotifyDto.getSn());
            return;
        }

        AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
        appTemplateQuery.setAppId(ele.getMerchantMinProAppId());
        appTemplateQuery.setSecret(ele.getMerchantMinProAppSecert());
        appTemplateQuery.setTouser(openId);
        appTemplateQuery.setFormId(RandomUtil.randomString(20));
        appTemplateQuery.setTemplateId(templateConfigEntity.getElectricQuantityRemindTemplate());
        appTemplateQuery.setPage("/pages/start/index");
        Map<String, Object> data = new HashMap<>(3);

        data.put("character_string1", batteryPowerNotifyDto.getSoc() + "%");
        data.put("character_string2", batteryPowerNotifyDto.getSn());
        //data.put("keyword3", sdf.format(new Date(System.currentTimeMillis())));
        data.put("thing3", "当前电量较低，请及时换电。");

        appTemplateQuery.setData(data);
        log.info("LOW BATTERY POWER MESSAGE TO USER uid={}, sn={}", uid, batteryPowerNotifyDto.getSn());

        weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);


    }
}
