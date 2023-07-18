package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.*;
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
public class NormalEleChargePowerHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    EleChargeConfigService eleChargeConfigService;

    @Autowired
    ElePowerService elePowerService;

    @Autowired
    StoreService storeService;

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

        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("NORMAL POWER ERROR! not found store! sessionId={},storeId={}", receiverMessage.getSessionId(), electricityCabinet.getStoreId());
            return;
        }

        ElePower power = new ElePower();
        power.setSn(electricityCabinet.getSn());
        power.setEName(electricityCabinet.getName());
        power.setEid(electricityCabinet.getId().longValue());
        power.setStoreId(electricityCabinet.getStoreId());
        power.setFranchiseeId(store.getFranchiseeId());
//        power.setTenantId();
//        power.setReportTime();
//        power.setCreateTime();
//        power.setType();
//        power.setSumPower();
//        power.setHourPower();
//        power.setElectricCharge();


    }


    @Data
    class CabinetPowerReport {
        private String powerConsumption;
        private String sumConsumption;
        private Long createTime;
        private String date;
    }
}


