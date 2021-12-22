package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.service.ApiOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalEleOrderOperateHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    @Autowired
    ApiOrderOperHistoryService apiOrderOperHistoryService;


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
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }

        EleOrderOperateVO eleOrderOperateVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOrderOperateVO.class);

        if (receiverMessage.getType().equalsIgnoreCase(HardwareCommand.API_ORDER_OPER_HISTORY)) {
            ApiOrderOperHistory history = ApiOrderOperHistory.builder()
                    .createTime(System.currentTimeMillis())
                    .orderId(eleOrderOperateVO.getOrderId())
                    .type(eleOrderOperateVO.getOrderType())
                    .tenantId(electricityCabinet.getTenantId())
                    .msg(eleOrderOperateVO.getMsg())
                    .build();
            apiOrderOperHistoryService.insert(history);
        } else {
            //加入操作记录表
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .createTime(System.currentTimeMillis())
                    .orderId(eleOrderOperateVO.getOrderId())
                    .type(eleOrderOperateVO.getOrderType())
                    .tenantId(electricityCabinet.getTenantId())
                    .msg(eleOrderOperateVO.getMsg())
                    .build();
            electricityCabinetOrderOperHistoryService.insert(history);
        }

        return true;
    }

}


@Data
class EleOrderOperateVO {

    //订单Id
    private String orderId;
    //
    private Long createTime;
    //
    private Integer orderType;
    //msg
    private String msg;
}
