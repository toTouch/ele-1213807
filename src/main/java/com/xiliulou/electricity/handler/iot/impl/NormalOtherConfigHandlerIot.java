package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
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

        Map<String, Object> map = JsonUtil.fromJson(receiverMessage.getOriginContent(), Map.class);
        if (Objects.isNull(map)) {
            log.error("other config error! no map,{}", receiverMessage.getOriginContent());
            return ;
        }
        //上报的数据放入缓存
//        redisService.saveWithHash(ElectricityCabinetConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId(), map);
        redisService.saveWithString(CacheConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId(), map);
    }

}
