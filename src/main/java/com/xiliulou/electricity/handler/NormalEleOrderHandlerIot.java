package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.dto.EleOpenDTO.EleOpenDTOBuilder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import shaded.org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalEleOrderHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    EleOperateQueueHandler eleOperateQueueHandler;

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
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return false;
        }
        //幂等加锁
        Boolean result = redisService.setNx(ElectricityCabinetConstant.ELE_RECEIVER_CACHE_KEY + sessionId, "true", 10 * 1000L, true);
        if (!result) {
            log.error("sessionId is lock,{}", sessionId);
            return false;
        }
        EleOpenDTOBuilder builder = EleOpenDTO.builder();

        EleOrderVo eleOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOrderVo.class);

        //操作回调的放在redis中 只有开门命令放入
        if (Objects.equals(receiverMessage.getType(), HardwareCommand.ELE_COMMAND_ORDER_NEW_DOOR_OPEN) || Objects.equals(receiverMessage.getType(), HardwareCommand.ELE_COMMAND_ORDER_OLD_DOOR_OPEN)) {
            if (Objects.nonNull(eleOrderVo.getStatus()) && eleOrderVo.getStatus().equals(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_FAIL)) {
                redisService.set(ElectricityCabinetConstant.ELE_OPERATOR_CACHE_KEY + sessionId, "true", 60L, TimeUnit.SECONDS);
            } else {
                redisService.set(ElectricityCabinetConstant.ELE_OPERATOR_CACHE_KEY + sessionId, "false", 60L, TimeUnit.SECONDS);
                redisService.set(ElectricityCabinetConstant.ELE_ORDER_OPERATOR_CACHE_KEY+eleOrderVo.getOrderId(), "false", 60L, TimeUnit.SECONDS);
            }
        }

        EleOpenDTO eleOpenDTO = builder
                .sessionId(sessionId)
                .type(receiverMessage.getType())
                .orderStatus(eleOrderVo.getOrderStatus())
                .status(eleOrderVo.getStatus())
                .orderId(eleOrderVo.getOrderId())
                .msg(eleOrderVo.getMsg())
                .productKey(receiverMessage.getProductKey())
                .deviceName(receiverMessage.getDeviceName()).build();
        eleOperateQueueHandler.putQueue(eleOpenDTO);
        log.info("eleOpenDTO is -->{}", eleOpenDTO);
        return true;
    }
}

@Data
class EleOrderVo {
    //sessionId
    private String sessionId;
    //productKey
    private String productKey;
    //orderId
    private String orderId;
    //msg
    private String msg;
    //orderStatus
    private Integer orderStatus;
    //status
    private Integer status;
}
