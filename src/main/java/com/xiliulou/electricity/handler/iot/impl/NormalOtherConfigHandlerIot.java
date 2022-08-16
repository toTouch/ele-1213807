package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @author: lxc
 * @Date: 2021/03/30 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_OTHER_CONFIG_HANDLER)
@Slf4j
public class NormalOtherConfigHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        ElectricityCabinetOtherSetting otherSetting = JsonUtil.fromJson(receiverMessage.getOriginContent(), ElectricityCabinetOtherSetting.class);
        if (Objects.isNull(otherSetting)) {
            log.error("OTHER CONFIG ERROR! sessionId={}", receiverMessage.getSessionId());
            return ;
        }
        //上报的数据放入缓存
//        redisService.saveWithString(CacheConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId(), map);
        redisService.saveWithHash(CacheConstant.OTHER_CONFIG_CACHE_V_2 + electricityCabinet.getId(), otherSetting);
    }

}
