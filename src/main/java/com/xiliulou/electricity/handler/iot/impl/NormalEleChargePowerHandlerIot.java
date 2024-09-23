package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.EleChargeConfigCalcDetailDto;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


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

    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.warn("no sessionId,{}", receiverMessage.getOriginContent());
            return;
        }

        CabinetPowerReport cabinetPowerReport = JsonUtil.fromJson(receiverMessage.getOriginContent(), CabinetPowerReport.class);
        if (Objects.isNull(cabinetPowerReport)) {
            log.warn("NORMAL POWER WARN! parse  power error! originContent={}", receiverMessage.getOriginContent());
            return;
        }

        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("NORMAL POWER WARN! not found store! sessionId={},storeId={}", receiverMessage.getSessionId(), electricityCabinet.getStoreId());
            return;
        }

        //电费单价
        Double unitPrice = 0.0;
        Integer chargeConfigType = EleChargeConfig.TYPE_NONE;

        EleChargeConfig eleChargeConfig = eleChargeConfigService.queryConfigByCabinetWithLayer(electricityCabinet, store.getFranchiseeId());
        if (Objects.nonNull(eleChargeConfig)) {
            EleChargeConfigCalcDetailDto dto = eleChargeConfigService.acquireConfigTypeAndUnitPriceAccrodingTime(eleChargeConfig, cabinetPowerReport.getCreateTime());
            if (Objects.nonNull(dto)) {
                unitPrice = dto.getPrice();
                chargeConfigType = dto.getType();
            }
        }
        
        // 抛掉重复上报的数据，直接发送确认命令
        Integer exits = elePowerService.exitsByEidAndReportTime(electricityCabinet.getId().longValue(), cabinetPowerReport.getCreateTime());
        if (Objects.nonNull(exits)) {
            sendConfirmationCommand(electricityCabinet, receiverMessage, cabinetPowerReport);
            return;
        }
        
        ElePower lastElePower = elePowerService.queryLatestByEid(electricityCabinet.getId().longValue());
        // 更换电表优化，更换电表之后sumPower需要继续累计
        double hourPower;
        double sumPower;
        if (Objects.nonNull(lastElePower)) {
            hourPower = Math.max(cabinetPowerReport.getPowerConsumption(), 0.00);
            sumPower = BigDecimal.valueOf(lastElePower.getSumPower()).add(BigDecimal.valueOf(hourPower)).setScale(2, RoundingMode.HALF_UP).doubleValue();
        } else {
            // 第一条上报
            hourPower = cabinetPowerReport.getPowerConsumption();
            sumPower = cabinetPowerReport.getSumConsumption();
        }
        
        
        ElePower power = new ElePower();
        power.setSn(electricityCabinet.getSn());
        power.setEName(electricityCabinet.getName());
        power.setEid(electricityCabinet.getId().longValue());
        power.setStoreId(electricityCabinet.getStoreId());
        power.setFranchiseeId(store.getFranchiseeId());
        power.setTenantId(electricityCabinet.getTenantId());
        power.setReportTime(cabinetPowerReport.getCreateTime());
        power.setCreateTime(System.currentTimeMillis());
        power.setType(chargeConfigType);
        power.setSumPower(sumPower);
        power.setHourPower(hourPower);
        power.setUnitPrice(unitPrice);
        power.setElectricCharge(BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(power.getHourPower())).setScale(2, RoundingMode.HALF_UP).doubleValue());
        power.setMeterReading(cabinetPowerReport.getSumConsumption());
        power.setOriginalHourPower(cabinetPowerReport.getPowerConsumption());
        
        elePowerService.insertOrUpdate(power);
        
        sendConfirmationCommand(electricityCabinet, receiverMessage, cabinetPowerReport);
    }
    
    private void sendConfirmationCommand(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage, CabinetPowerReport cabinetPowerReport) {
        //发送命令确认
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("time", cabinetPowerReport.getCreateTime());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", "")).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.CALC_ELE_POWER_REPORT_ACK).build();
        
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (!sendResult.getLeft()) {
            log.error("NORMAL POWER ERROR! send command error! sessionid:{}", receiverMessage.getSessionId());
        }
    }


}


@Data
class CabinetPowerReport {
    private Double powerConsumption;
    private Double sumConsumption;
    private Long createTime;
}