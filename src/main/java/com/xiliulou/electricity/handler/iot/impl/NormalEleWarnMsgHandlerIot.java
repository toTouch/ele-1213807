package com.xiliulou.electricity.handler.iot.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.mq.producer.MessageSendProducer;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.EleCommonConfig;
import com.xiliulou.electricity.config.TenantConfig;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.*;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.mq.service.RocketMqService;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import shaded.org.apache.commons.lang3.StringUtils;

/**
 * @author: hrp
 * @Date: 2022/09/20 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_WARN_MSG_HANDLER)
@Slf4j
public class NormalEleWarnMsgHandlerIot extends AbstractElectricityIotHandler {
    
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    BatteryOtherPropertiesService batteryOtherPropertiesService;
    
    @Autowired
    NotExistSnService notExistSnService;
    
    @Autowired
    ClickHouseService clickHouseService;
    
    @Autowired
    EleCommonConfig eleCommonConfig;
    
    @Autowired
    TenantConfig tenantConfig;
    
    @Autowired
    RocketMqService rocketMqService;
    
    @Autowired
    MessageSendProducer messageSendProducer;
    
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    public static final Integer CELL_ERROR_TYPE = 1;
    
    public static final Integer BATTERY_ERROR_TYPE = 2;
    
    public static final Integer CABINET_ERROR_TYPE = 3;
    
    public static final Integer BUSINESS_ERROR_TYPE = 4;
    
    // 柜机上报的error_code  80004：烟雾告警 ，80008:后门异常打开
    public static final Long SMOKE_WARN_ERROR_CODE = 80004L;
    
    public static final Long BACK_DOOR_OPEN_ERROR_CODE = 80008L;
    
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("ELE ERROR! warnMsgReport NO sessionId,{}", receiverMessage.getSessionId());
            return;
        }
        
        EleWarnMsgVo eleWarnMsgVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleWarnMsgVo.class);
        if (Objects.isNull(eleWarnMsgVo)) {
            log.error("ELE ERROR! warnMsgReport is null,sessionId={}", receiverMessage.getSessionId());
            return;
        }
        
        if (Objects.equals(eleWarnMsgVo.getErrorType(), CELL_ERROR_TYPE)) {
            saveCellWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        } else if (Objects.equals(eleWarnMsgVo.getErrorType(), BATTERY_ERROR_TYPE)) {
            saveBatteryWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        } else if (Objects.equals(eleWarnMsgVo.getErrorType(), CABINET_ERROR_TYPE)) {
            saveCabinetWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        } else if (Objects.equals(eleWarnMsgVo.getErrorType(), BUSINESS_ERROR_TYPE)) {
            saveBusinessWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        }
        
        //烟雾告警、后门异常打开  故障上报发送通知
        //        this.sendWarnMessageNotify(electricityCabinet, eleWarnMsgVo);
        
    }
    
    
    /**
     * 电池故障保存到clickhouse
     *
     * @param
     */
    private void saveBatteryWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {
        
        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);
        
        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);
        
        String sql = "insert into t_warn_msg_battery (electricityCabinetId,errorCode,sessionId,batteryName,errorMsg,createTime,reportTime,tenantId) values(?,?,?,?,?,?,?,?);";
        
        try {
            clickHouseService
                    .insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getErrorCode(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getBatteryName(), eleWarnMsgVo.getErrorMsg(),
                            createTime, reportTime, electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert batteryWarn sql error!", e);
        }
    }
    
    /**
     * 仓门故障保存到clickhouse
     *
     * @param
     */
    private void saveCellWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {
        
        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);
        
        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);
        
        String sql = "insert into t_warn_msg_cell (electricityCabinetId,errorCode,sessionId,cellNo,errorMsg,createTime,reportTime,operateType,tenantId) values(?,?,?,?,?,?,?,?,?);";
        
        try {
            clickHouseService
                    .insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getErrorCode(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getErrorMsg(),
                            createTime, reportTime, eleWarnMsgVo.getOperateType(), electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert cellWarn sql error!", e);
        }
    }
    
    /**
     * 柜机故障保存到clickhouse
     *
     * @param
     */
    private void saveCabinetWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {
        
        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);
        
        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);
        
        String sql = "insert into t_warn_msg_cabinet (electricityCabinetId,errorCode,sessionId,operateType,errorMsg,createTime,reportTime,tenantId) values(?,?,?,?,?,?,?,?);";
        
        try {
            clickHouseService
                    .insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getErrorCode(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getOperateType(), eleWarnMsgVo.getErrorMsg(),
                            createTime, reportTime, electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert cabinetWarn sql error!", e);
        }
    }
    
    /**
     * 业务故障保存到clickhouse
     *
     * @param
     */
    private void saveBusinessWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {
        
        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);
        
        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);
        
        String sql = "insert into t_warn_msg_business (electricityCabinetId,errorCode,sessionId,cellNo,errorMsg,createTime,reportTime,tenantId) values(?,?,?,?,?,?,?,?);";
        
        try {
            clickHouseService
                    .insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getErrorCode(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getErrorMsg(),
                            createTime, reportTime, electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert cabinetWarn sql error!", e);
        }
    }
    
    /**
     * 故障上报发送MQ通知
     *
     * @param electricityCabinet
     * @param eleWarnMsgVo
     */
    private void sendWarnMessageNotify(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {
        List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> messageNotifyList = null;
        
        if (Objects.equals(SMOKE_WARN_ERROR_CODE, eleWarnMsgVo.getErrorCode())) {
            messageNotifyList = this
                    .buildWarnMessageNotify(electricityCabinet, eleWarnMsgVo, ElectricityAbnormalMessageNotify.SMOKE_WARN_TYPE, ElectricityAbnormalMessageNotify.SMOKE_WARN_MSG);
        } else if (Objects.equals(BACK_DOOR_OPEN_ERROR_CODE, eleWarnMsgVo.getErrorCode())) {
            messageNotifyList = this.buildWarnMessageNotify(electricityCabinet, eleWarnMsgVo, ElectricityAbnormalMessageNotify.BACK_DOOR_OPEN_TYPE,
                    ElectricityAbnormalMessageNotify.BACK_DOOR_OPEN_MSG);
        } else {
            return;
        }
        
        if (!CollectionUtils.isEmpty(messageNotifyList)) {
            messageNotifyList.forEach(item -> {
                messageSendProducer.sendAsyncMsg(item, "", "", 0);
                log.info("ELE WARN MSG INFO! ele warn message notify, msg={}", JsonUtil.toJson(item));
            });
        }
    }
    
    private List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> buildWarnMessageNotify(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo, Integer warnNotifyType,
            String description) {
        
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.error("ELE WARN MSG ERROR! not found maintenanceUserNotifyConfig,tenantId={}", electricityCabinet.getTenantId());
            return Collections.EMPTY_LIST;
        }
        
        List<String> phones = JSON.parseObject(notifyConfig.getPhones(), List.class);
        if (CollectionUtils.isEmpty(phones)) {
            log.error("ELE WARN MSG ERROR! phones is empty,tenantId={}", electricityCabinet.getTenantId());
            return Collections.EMPTY_LIST;
        }
        
        return phones.parallelStream().map(item -> {
            ElectricityAbnormalMessageNotify messageNotify = new ElectricityAbnormalMessageNotify();
            messageNotify.setAddress(electricityCabinet.getAddress());
            messageNotify.setEquipmentNumber(electricityCabinet.getName());
            messageNotify.setDescription(description);
            messageNotify.setExceptionType(warnNotifyType);
            messageNotify.setReportTime(formatter.format(LocalDateTime.now()));
            
            MqNotifyCommon<ElectricityAbnormalMessageNotify> abnormalMessageNotifyCommon = new MqNotifyCommon<>();
            abnormalMessageNotifyCommon.setTime(System.currentTimeMillis());
            abnormalMessageNotifyCommon.setType(SendMessageTypeEnum.ABNORMAL_ALARM_NOTIFY.getType());
            abnormalMessageNotifyCommon.setPhone(item);
            abnormalMessageNotifyCommon.setData(messageNotify);
            abnormalMessageNotifyCommon.setTenantId(electricityCabinet.getTenantId());
            
            return abnormalMessageNotifyCommon;
        }).collect(Collectors.toList());
    }
    
    @Data
    class EleWarnMsgVo {
        
        private String sessionId;
        
        private Integer errorType;
        
        private Long createTime;
        
        private Long errorCode;
        
        private String batteryName;
        
        private String errorMsg;
        
        private Integer cellNo;
        
        private Integer operateType;
    }
    
}




