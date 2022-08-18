package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetTrafficService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * zgw
 */
@Service(value= ElectricityIotConstant.NORMAL_SIM_TRAFFIC_STATISTICS_HANDLER)
@Slf4j
public class NormalSimTrafficStatisticsHandler extends AbstractElectricityIotHandler {

    @Autowired
    ElectricityCabinetTrafficService electricityCabinetTrafficService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return ;
        }

        SimCardTrafficStatisticsRequest request = JsonUtil.fromJson(receiverMessage.getOriginContent(), SimCardTrafficStatisticsRequest.class);
        if (Objects.isNull(request)) {
            log.error("NORMAL SimTrafficStatistics ERROR! parse SimCardTrafficStatisticsRequest error! originContent={}", receiverMessage.getOriginContent());
            return ;
        }

        if (!request.isSuccess()) {
            log.warn("NORMAL SimTrafficStatistics WARN! simCardTrafficStatisticsRequest consumption warn! originContent={}", receiverMessage.getOriginContent());
            return ;
        }

        ElectricityCabinetTraffic electricityCabinetTraffic = new ElectricityCabinetTraffic();
        electricityCabinetTraffic.setEid(electricityCabinet.getId().longValue());
        electricityCabinetTraffic.setSameDayTraffic(Double.parseDouble(request.getTrafficStatistics()));
        electricityCabinetTraffic.setSumTraffic(Double.parseDouble(request.getSumTrafficStatistics()));
        electricityCabinetTraffic.setDate(LocalDate.parse(request.getDate(), formatter));
        electricityCabinetTraffic.setTenantId(electricityCabinet.getTenantId());
        electricityCabinetTraffic.setCreateTime(System.currentTimeMillis());
        electricityCabinetTraffic.setUpdateTime(System.currentTimeMillis());
        electricityCabinetTrafficService.insertOrUpdate(electricityCabinetTraffic);
    }
}

@Data
class SimCardTrafficStatisticsRequest{
    private String sessionId;
    private String trafficStatistics;
    private String date;
    private String sumTrafficStatistics;
    private boolean success;
}