package com.xiliulou.electricity.handler.iot.impl;

import com.google.gson.JsonElement;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.BatteryMultiConfigDTO;
import com.xiliulou.electricity.entity.BatteryChargeConfig;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import com.xiliulou.electricity.service.BatteryChargeConfigService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 电池充电设置
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-15-17:32
 */
@Service(value = ElectricityIotConstant.NORMAL_BATTERY_MULTI_SETTING_HANDLER)
@Slf4j
public class NormalEleBatteryMultiSettingHandler extends AbstractElectricityIotHandler {

    @Autowired
    private BatteryChargeConfigService batteryChargeConfigService;
    @Autowired
    private RedisService redisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        JsonElement jsonElement = JsonUtil.fetchJsonElement(receiverMessage.getOriginContent(), "list");
        if (Objects.isNull(jsonElement)) {
            log.error("ELE BATTERY SETTING ERROR! report list is null,sessionId={}", receiverMessage.getSessionId());
            return;
        }

        List<BatteryMultiConfigDTO> batteryMultiConfigVOS = JsonUtil.fromJsonArray(jsonElement.getAsString(), BatteryMultiConfigDTO.class);
        if (CollectionUtils.isEmpty(batteryMultiConfigVOS)) {
            log.error("ELE BATTERY SETTING ERROR! batteryMultiConfigVOS is empty,sessionId={}", receiverMessage.getSessionId());
            return;
        }

        //获取柜机模式
        String applicationModel = null;
        try {
//            ElectricityCabinetOtherSetting eleOtherSetting = redisService.getWithHash(CacheConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId(), ElectricityCabinetOtherSetting.class);
//            if (Objects.nonNull(eleOtherSetting)) {
//                applicationModel = eleOtherSetting.getApplicationMode();
//            }
// TODO 阶段一更新后


            String result = redisService.get(CacheConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId());
            if (StringUtils.isEmpty(result)) {
                log.error("ELE BATTERY REPORT ERROR! result is null,sessionId={}", receiverMessage.getSessionId());
                return;
            }
            Map<String, String> map = JsonUtil.fromJson(result, Map.class);
            applicationModel = map.getOrDefault("applicationMode", "-1");
        } catch (Exception e) {
            log.error("ELE BATTERY REPORT ERROR! applicationMode parsing failed,electricityCabinetId={},sessionId={}", electricityCabinet.getId(), receiverMessage.getSessionId());
        }

        BatteryChargeConfigQuery batteryChargeConfigQuery = new BatteryChargeConfigQuery();
        batteryChargeConfigQuery.setApplicationModel(applicationModel);
        batteryChargeConfigQuery.setConfigList(batteryMultiConfigVOS);
        batteryChargeConfigQuery.setElectricityCabinetId(electricityCabinet.getId().longValue());
        batteryChargeConfigQuery.setDelFlag(BatteryChargeConfig.DEL_NORMAL);
        batteryChargeConfigQuery.setCreateTime(System.currentTimeMillis());
        batteryChargeConfigQuery.setUpdateTime(System.currentTimeMillis());
        batteryChargeConfigService.atomicUpdate(batteryChargeConfigQuery);

    }
}
