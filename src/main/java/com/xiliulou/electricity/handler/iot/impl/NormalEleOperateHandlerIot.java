package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_ELE_OPERATE_HANDLER)
@Slf4j
public class NormalEleOperateHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return ;
        }
        Map<String, Object> map = JsonUtil.fromJson(receiverMessage.getOriginContent(), Map.class);

//        //操作回调的放在redis中
//        if (Objects.nonNull(receiverMessage.getSuccess()) && "True".equalsIgnoreCase(receiverMessage.getSuccess())) {
//            redisService.saveWithString(CacheConstant.ELE_OPERATOR_CACHE_KEY + sessionId, operateVo, 30L, TimeUnit.SECONDS);
//        } else {
//            redisService.saveWithString(CacheConstant.ELE_OPERATOR_CACHE_KEY + sessionId, operateVo, 30L, TimeUnit.SECONDS);
//        }
        redisService.saveWithString(CacheConstant.ELE_OPERATOR_CACHE_KEY + sessionId, map, 30L, TimeUnit.SECONDS);
    }

}
