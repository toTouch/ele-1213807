package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
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

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalEleExchangeHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    ExecutorService executorService = Executors.newFixedThreadPool(2);

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
        executorService.execute(() -> {
            if (!StrUtil.isNotEmpty(receiverMessage.getVersion())) {
                return;
            }
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
                return;
            }
            //版本号修改
            ElectricityCabinet newElectricityCabinet = new ElectricityCabinet();
            newElectricityCabinet.setId(electricityCabinet.getId());
            newElectricityCabinet.setVersion(receiverMessage.getVersion());
            if (electricityCabinetService.update(newElectricityCabinet) > 0) {
                redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + newElectricityCabinet.getId());
                redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            }
            log.error("type is exchange_cabinet,{}", receiverMessage.getOriginContent());
        });

        return true;
    }

}
