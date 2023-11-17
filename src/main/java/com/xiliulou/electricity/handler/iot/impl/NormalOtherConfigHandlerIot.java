package com.xiliulou.electricity.handler.iot.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.dto.OtherSettingParamTemplateRequestDTO;
import com.xiliulou.electricity.entity.BatteryChargeConfig;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import com.xiliulou.electricity.service.BatteryChargeConfigService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @author: lxc
 * @Date: 2021/03/30 17:02
 * @Description:
 */
//
@Service(value= ElectricityIotConstant.NORMAL_OTHER_CONFIG_HANDLER)
@Slf4j
public class NormalOtherConfigHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    BatteryChargeConfigService batteryChargeConfigService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
    
        OtherSettingParamTemplateRequestDTO msg = JsonUtil
                .fromJson(receiverMessage.getOriginContent(), OtherSettingParamTemplateRequestDTO.class);
    
        //ElectricityCabinetOtherSetting otherSetting = JsonUtil.fromJson(receiverMessage.getOriginContent(), ElectricityCabinetOtherSetting.class);
        if (Objects.isNull(msg)) {
            log.error("OTHER CONFIG ERROR! sessionId={}", receiverMessage.getSessionId());
            return ;
        }
    
        ElectricityCabinetOtherSetting otherSetting = new ElectricityCabinetOtherSetting();
        BeanUtils.copyProperties(msg, otherSetting);
        
        redisService.saveWithHash(CacheConstant.OTHER_CONFIG_CACHE_V_2 + electricityCabinet.getId(), otherSetting);

        //APP端修改了换电标准  saas平台同步更新
        if(StringUtils.isNotBlank(otherSetting.getExchangeCondition())){
            ElectricityCabinet electricityCabinetUpdate=new ElectricityCabinet();
            electricityCabinetUpdate.setId(electricityCabinet.getId());
            electricityCabinetUpdate.setProductKey(electricityCabinet.getProductKey());
            electricityCabinetUpdate.setDeviceName(electricityCabinet.getDeviceName());
            electricityCabinetUpdate.setFullyCharged(Double.valueOf(otherSetting.getExchangeCondition()));
            electricityCabinetUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetService.update(electricityCabinetUpdate);
        }

        if (CollectionUtils.isEmpty(msg.getSettingParamTemplate())) {
            return;
        }
    
        BatteryChargeConfigQuery batteryChargeConfigQuery = new BatteryChargeConfigQuery();
        batteryChargeConfigQuery.setApplicationModel(otherSetting.getApplicationMode());
        batteryChargeConfigQuery.setConfigList(msg.getSettingParamTemplate());
        batteryChargeConfigQuery.setElectricityCabinetId(electricityCabinet.getId().longValue());
        batteryChargeConfigQuery.setDelFlag(BatteryChargeConfig.DEL_NORMAL);
        batteryChargeConfigQuery.setTenantId(electricityCabinet.getTenantId());
        batteryChargeConfigQuery.setCreateTime(System.currentTimeMillis());
        batteryChargeConfigQuery.setUpdateTime(System.currentTimeMillis());
        batteryChargeConfigService.insertOrUpdate(batteryChargeConfigQuery);
        redisService.saveWithString(CacheConstant.ELE_OPERATOR_CACHE_KEY + receiverMessage.getSessionId(), "ok", 30L,
                TimeUnit.SECONDS);
    }

}
