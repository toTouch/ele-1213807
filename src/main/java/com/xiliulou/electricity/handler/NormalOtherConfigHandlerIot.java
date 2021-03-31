package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllExecutors;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.tuple.Pair;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


/**
 * @author: lxc
 * @Date: 2021/03/30 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalOtherConfigHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Override
    protected Pair<SendHardwareMessage, String> generateMsg(HardwareCommandQuery hardwareCommandQuery) {
        String sessionId = generateSessionId(hardwareCommandQuery);
        SendHardwareMessage message = SendHardwareMessage.builder()
                .sessionId(sessionId)
                .type(hardwareCommandQuery.getCommand())
                .data(hardwareCommandQuery.getData()).build();
        return Pair.of(message, sessionId);
    }

    @Override
    protected boolean receiveMessageProcess(ReceiverMessage receiverMessage) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }
        Map<String, Object> map = JsonUtil.fromJson(receiverMessage.getOriginContent(), Map.class);
        if (Objects.isNull(map)) {
            log.error("other config error! no map,{}", receiverMessage.getOriginContent());
            return false;
        }
        //上报的数据放入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId(), map);
        return true;
    }

}
