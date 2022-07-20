package com.xiliulou.electricity.handler.iot;

import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import org.apache.commons.lang3.tuple.Pair;

public interface IElectricityHandler {

    Pair<Boolean, String> handleSendHardwareCommand(HardwareCommandQuery hardwareCommandQuery);

    boolean receiveMessageProcess(ReceiverMessage receiverMessage);

}
