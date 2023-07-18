package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.EleChargeConfigService;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.ElectricityCabinetPowerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@Service(value = ElectricityIotConstant.NORMAL_ELE_CHARGE_POWER_HANDLER)
@Slf4j
@Deprecated
public class NormalEleChargePowerHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    EleChargeConfigService eleChargeConfigService;

    @Autowired
    ElePowerService elePowerService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return;
        }

        CabinetPowerReport cabinetPowerReport = JsonUtil.fromJson(receiverMessage.getOriginContent(), CabinetPowerReport.class);
        if (Objects.isNull(cabinetPowerReport)) {
            log.error("NORMAL POWER ERROR! parse  power error! originContent={}", receiverMessage.getOriginContent());
            return;
        }





    }


    @Data
    class CabinetPowerReport {
        private String powerConsumption;
        private String sumConsumption;
        private Long createTime;
        private String date;
    }
}


