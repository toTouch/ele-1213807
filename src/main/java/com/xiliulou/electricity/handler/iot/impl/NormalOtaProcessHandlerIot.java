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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author zgw
 * @date 2022/8/17 16:14
 * @mood
 */
@Service(value= ElectricityIotConstant.NORMAL_OTA_PROCESS_HANDLER)
@Slf4j
public class NormalOtaProcessHandlerIot extends AbstractElectricityIotHandler {

    @Autowired
    RedisService redisService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return ;
        }

        OtaRequest request = JsonUtil.fromJson(receiverMessage.getOriginContent(), OtaRequest.class);
        if(Objects.isNull(request)) {
            log.warn("OTA_PROCESS ERROR! parse OtaRequest error! sessionId={}, productKey={}, deviceName={}",receiverMessage.getSessionId(), receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return;
        }

        redisService.saveWithHash(CacheConstant.OTA_PROCESS_CACHE + sessionId, request);
        redisService.expire(CacheConstant.OTA_PROCESS_CACHE + sessionId, 30L * 1000 , false);
    }
}

@Data
class OtaRequest {
    private String type;
    private String sessionId;
    private Integer upgradeType;
    private Long completeTime;
    private List<Integer> successCells;
    private List<Integer> failCells;
    private Boolean coreUpgradeResult;
    private Boolean operateResult;
    private String msg;
    private String productKey;
    private String deviceName;
}
