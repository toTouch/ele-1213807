package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * zgw
 */
@Service(value= ElectricityIotConstant.NORMAL_SIM_TRAFFIC_STATISTICS_HANDLER)
@Slf4j
public class NormalSimTrafficStatisticsHandler extends AbstractElectricityIotHandler {

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

    }
}
