package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : eclair
 * @date : 2023/7/27 10:56
 */
@Service(value = ElectricityIotConstant.NEW_HARDWARE_WARN_MSG_HANDLER)
@Slf4j
public class NewHardwareWarnMsgHandler extends AbstractElectricityIotHandler {
    @Override
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        super.postHandleReceiveMsg(electricityCabinet, receiverMessage);
    }
}
@Data
class HardwareWarnMsg {
    private Long createTime;
    private Long endTime;
    private Integer errorCode;
    private Long triggerCount;
}
