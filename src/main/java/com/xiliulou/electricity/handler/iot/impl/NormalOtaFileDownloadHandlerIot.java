package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author zgw
 * @date 2022/10/12 18:45
 * @mood
 */
@Service(value = ElectricityIotConstant.NORMAL_OTA_FILE_DOWNLOAD_HANDLER)
@Slf4j
public class NormalOtaFileDownloadHandlerIot extends AbstractElectricityIotHandler {
    
    @Autowired
    RedisService redisService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return;
        }
        
        //操作回调的放在redis中
        if (Objects.nonNull(receiverMessage.getSuccess()) && "true".equalsIgnoreCase(receiverMessage.getSuccess())) {
            redisService.set(CacheConstant.OTA_FILE_DOWNLOAD_CACHE_KEY + sessionId, "true", 30L, TimeUnit.SECONDS);
        } else {
            redisService.set(CacheConstant.OTA_FILE_DOWNLOAD_CACHE_KEY + sessionId, "false", 30L, TimeUnit.SECONDS);
        }
    }
}
