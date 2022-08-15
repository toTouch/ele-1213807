package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author zgw
 * @date 2022/8/15 16:18
 * @mood
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_ORDER_FRANCHISEES_NOT_SAME_HANDLER)
@Slf4j
public class NormalEleFranchiseesNotSameHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        /*String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("FRANCHISEES NOT SAME ERROR! sessionId is null error! originContent={}", receiverMessage.getOriginContent());
            return;
        }

        //幂等加锁
        Boolean result = redisService.setNx(CacheConstant.FRANCHISEES_NOT_SAME_OPEN_DOOR_LOCK + sessionId, "true", 5 * 1000L, true);
        if (!result) {
            log.error("FRANCHISEES NOT SAME ERROR! setNx lock error!,sessionId={}", sessionId);
            return;
        }

        //操作回调的放在redis中,记录开门结果
        if (Objects.nonNull(receiverMessage.getSuccess()) && "True".equalsIgnoreCase(receiverMessage.getSuccess())) {
            redisService.set(CacheConstant.FRANCHISEES_NOT_SAME_OPEN_DOOR + sessionId, "true", 30L, TimeUnit.SECONDS);
        } else {
            redisService.set(CacheConstant.FRANCHISEES_NOT_SAME_OPEN_DOOR + sessionId, "false", 30L, TimeUnit.SECONDS);
        }*/
    }
}
