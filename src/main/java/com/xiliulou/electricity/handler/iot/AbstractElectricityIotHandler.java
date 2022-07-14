package com.xiliulou.electricity.handler.iot;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.PubHardwareService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.UUID;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-06-13-15:50
 */
@Slf4j
public abstract class AbstractElectricityIotHandler implements IElectricityHandler {

    @Autowired
    PubHardwareService pubHardwareService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;


    @Override
    public Pair<Boolean, String> handleSendHardwareCommand(HardwareCommandQuery hardwareCommandQuery) {
        Pair<SendHardwareMessage, String> message = this.generateMsg(hardwareCommandQuery);
        return Pair.of(pubHardwareService.sendMessage(hardwareCommandQuery.getProductKey(), hardwareCommandQuery.getDeviceName(), message.getLeft()), message.getRight());
    }

    protected Pair<SendHardwareMessage, String> generateMsg(HardwareCommandQuery hardwareCommandQuery) {
        String sessionId = generateSessionId(hardwareCommandQuery);
        SendHardwareMessage message = SendHardwareMessage.builder()
                .sessionId(sessionId)
                .type(hardwareCommandQuery.getCommand())
                .data(hardwareCommandQuery.getData()).build();
        return Pair.of(message, sessionId);
    }

    protected String generateSessionId(HardwareCommandQuery hardwareCommandQuery) {
        if (StrUtil.isEmpty(hardwareCommandQuery.getSessionId())) {
            String var10000 = UUID.randomUUID().toString().replaceAll("-", "");
            return var10000 + "_" + hardwareCommandQuery.getDeviceName();
        } else {
            return hardwareCommandQuery.getSessionId();
        }
    }

    @Override
    public boolean receiveMessageProcess(ReceiverMessage receiverMessage) {

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE ERROR! not found  electricity ,productKey={},deviceName={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }

        postHandleReceiveMsg(electricityCabinet, receiverMessage);

        return true;
    }


    /**
     * 子类根据需要继承处理
     *
     * @param electricityCabinet
     * @param receiverMessage
     */
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

    }
}
