package com.xiliulou.electricity.handler.iot.impl;

import com.google.common.collect.Maps;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.mq.service.RocketMqService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2023/7/27 10:56
 */
@Service(value = ElectricityIotConstant.NEW_HARDWARE_WARN_MSG_HANDLER)
@Slf4j
public class NewHardwareWarnMsgHandler extends AbstractElectricityIotHandler {
    @Autowired
    RocketMqService rocketMqService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;

    @Override
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        HardwareWarnMsg hardwareWarnMsg = JsonUtil.fromJson(receiverMessage.getOriginContent(), HardwareWarnMsg.class);
        if (Objects.isNull(hardwareWarnMsg)) {
            log.error("PARSE HARDWARE WARN MSG ERROR! sessionId={}", receiverMessage.getSessionId());
            return;
        }

        HardwareFailureMqMsg msg = new HardwareFailureMqMsg();
        if (hardwareWarnMsg.getErrorType().equals(HardwareWarnMsg.BATTERY_TYPE)) {
            msg.setSnType(HardwareFailureMqMsg.BATTERY_TYPE);
            msg.setSn(hardwareWarnMsg.getBatteryName());
        } else if(hardwareWarnMsg.getErrorType().equals(HardwareWarnMsg.BUSINESS_TYPE)) {
            msg.setSnType(HardwareFailureMqMsg.BUSINESS_TYPE);
            msg.setSn(electricityCabinet.getSn());
        } else {
            msg.setSnType(HardwareFailureMqMsg.CABINET_TYPE);
            msg.setSn(electricityCabinet.getSn());
        }
        msg.setAddress(electricityCabinet.getAddress());
        msg.setErrorCode(String.valueOf(hardwareWarnMsg.getErrorCode()));
        msg.setWarnTime(hardwareWarnMsg.getCreateTime());
        msg.setErrorDesc("格挡号:" + hardwareWarnMsg.getCellNo());

        rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_HARDWARE_FAILURE, JsonUtil.toJson(msg));

        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("requestId", receiverMessage.getSessionId());

        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(
                        receiverMessage.getSessionId())
                .deviceName(electricityCabinet.getDeviceName())
                .data(dataMap)
                .command(ElectricityIotConstant.NEW_HARDWARE_WARN_MSG_ACK).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("HARDWARE WARN MSG ERROR! send command error! requestId:{}", receiverMessage.getSessionId());
        }
    }
}

@Data
class HardwareWarnMsg {
    private Long createTime;
    private Long endTime;
    private Integer errorCode;
    private Long triggerCount;
    private Integer errorType;
    private String batteryName;
    private Integer cellNo;

    public static final Integer CELL_TYPE = 1;
    public static final Integer CABINET_TYPE = 3;
    public static final Integer BATTERY_TYPE = 2;
    public static final Integer BUSINESS_TYPE = 4;

}


@Data
class HardwareFailureMqMsg {
    private String sn;
    private String snType;
    private Long warnTime;
    private String errorCode;
    private String errorDesc;
    private String data;
    private String address;

    /**
     * 电池
     */
    public static final String BATTERY_TYPE = "DC";
    /**
     * 柜机
     */
    public static final String CABINET_TYPE = "GJ";
    /**
     * 业务
     */
    public static final String BUSINESS_TYPE = "BS";

}