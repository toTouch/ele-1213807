package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author BaoYu
 * @description:
 * @date 2023/12/13 11:31
 */

@Slf4j
@Service(value = ElectricityIotConstant.NORMAL_OFFLINE_EXCHANGE_PASSWORD_HANDLER)
public class NormalOfflineExchangePasswordHandler extends AbstractElectricityIotHandler {
    
    @Resource
    RedisService redisService;
    
    @Override
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        //该消息的接受响应，目前走了other config的消息处理。但是发送信息依然使用了当前的handler。
        //保留该handler，以防以后需要对柜机上报的密码信息做额外处理。
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return ;
        }
        Map<String, Object> map = JsonUtil.fromJson(receiverMessage.getOriginContent(), Map.class);
        
        log.info("offline exchange password ! data={}", map);
        redisService.saveWithString(CacheConstant.ELE_OPERATOR_CACHE_KEY + sessionId, map, 600L, TimeUnit.SECONDS);
    }
    
}
