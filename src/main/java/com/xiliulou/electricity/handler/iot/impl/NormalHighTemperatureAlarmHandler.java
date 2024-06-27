package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleHighTemperatureAlarmNotify;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.request.notify.SendNotifyMessageRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.notify.NotifyUserInfoService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-16-9:51
 */
@Slf4j
@Service(value = ElectricityIotConstant.NORMAL_HIGH_TEMPERATURE_ALARM_HANDLER)
public class NormalHighTemperatureAlarmHandler extends AbstractElectricityIotHandler {
    
    @Autowired
    NotifyUserInfoService notifyUserInfoService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    @Override
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        
        HighTemperatureAlarmVO highTemperatureAlarmVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), HighTemperatureAlarmVO.class);
        if (Objects.isNull(highTemperatureAlarmVO)) {
            log.warn("ELE HIGH TEMPERATURE WARN! highTemperatureAlarmVO is null,sessionId={}", sessionId);
            return;
        }
        
        List<SendNotifyMessageRequest<EleHighTemperatureAlarmNotify>> messageNotifyList = buildHighTemperatureAlarmNotify(electricityCabinet, highTemperatureAlarmVO);
        if (CollectionUtils.isEmpty(messageNotifyList)) {
            return;
        }
        
        messageNotifyList.forEach(i -> notifyUserInfoService.asyncSendMessage(i));
    }
    
    private List<SendNotifyMessageRequest<EleHighTemperatureAlarmNotify>> buildHighTemperatureAlarmNotify(ElectricityCabinet electricityCabinet,
            HighTemperatureAlarmVO highTemperatureAlarmVO) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.warn("ELE HIGH TEMPERATURE WARN! not found maintenanceUserNotifyConfig,tenantId={}", electricityCabinet.getTenantId());
            return Collections.emptyList();
        }
        
        if ((notifyConfig.getPermissions() & MaintenanceUserNotifyConfig.TYPE_HIGH_TEMPERATURE_ALARM) != MaintenanceUserNotifyConfig.TYPE_HIGH_TEMPERATURE_ALARM) {
            return Collections.emptyList();
        }
        
        List<String> phones = JsonUtil.fromJsonArray(notifyConfig.getPhones(), String.class);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(phones)) {
            log.warn("ELE HIGH TEMPERATURE WARN! phones is empty,tenantId={}", electricityCabinet.getTenantId());
            return Collections.emptyList();
        }
        
        return phones.parallelStream().map(item -> {
            EleHighTemperatureAlarmNotify abnormalMessageNotify = new EleHighTemperatureAlarmNotify();
            abnormalMessageNotify.setCabinetName(electricityCabinet.getName());
            abnormalMessageNotify.setCellNo(highTemperatureAlarmVO.getCellNo());
            abnormalMessageNotify.setCellHeat(highTemperatureAlarmVO.getCellHeat());
            abnormalMessageNotify.setDescription("柜机高温告警");
            abnormalMessageNotify.setReportTime(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_FORMAT));
            
            SendNotifyMessageRequest<EleHighTemperatureAlarmNotify> abnormalMessageNotifyCommon = new SendNotifyMessageRequest<>();
            abnormalMessageNotifyCommon.setTime(System.currentTimeMillis());
            abnormalMessageNotifyCommon.setType(SendMessageTypeEnum.HIGH_WARNING_NOTIFY);
            abnormalMessageNotifyCommon.setPhone(item);
            abnormalMessageNotifyCommon.setData(abnormalMessageNotify);
            abnormalMessageNotifyCommon.setTenantId(electricityCabinet.getTenantId());
            return abnormalMessageNotifyCommon;
        }).collect(Collectors.toList());
    }
    
    @Data
    class HighTemperatureAlarmVO {
        
        /**
         * 格挡
         */
        private Integer cellNo;
        
        /**
         * 格口温度
         */
        private Double cellHeat;
    }
}
