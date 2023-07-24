package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-24-14:39
 */
@Slf4j
@Service(value = ElectricityIotConstant.FULL_BATTERY_WARNING_HANDLER)
public class FullBatteryWarningHandler extends AbstractElectricityIotHandler {

    @Autowired
    private ElectricityCabinetService electricityCabinetService;

    @Autowired
    private RocketMqService rocketMqService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> messageNotifyList = electricityCabinetService.buildAbnormalMessageNotify(electricityCabinet);
        if (CollectionUtils.isEmpty(messageNotifyList)) {
            log.warn("FULL BATTERY WARN!messageNotifyList is empty,sessionId={},eid={}", receiverMessage.getSessionId(), electricityCabinet.getId());
            return;
        }

        messageNotifyList.forEach(i -> rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(i), "", "", 0));
    }
}
