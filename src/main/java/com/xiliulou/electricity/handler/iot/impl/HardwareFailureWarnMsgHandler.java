package com.xiliulou.electricity.handler.iot.impl;

import com.google.common.collect.Maps;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.mq.service.RocketMqService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author : maxiaodong
 * @date : 2023/12/26 10:56
 */
@Service(value = ElectricityIotConstant.HARDWARE_FAILURE_WARN_MSG_HANDLER)
@Slf4j
public class HardwareFailureWarnMsgHandler extends AbstractElectricityIotHandler {
    
    @Autowired
    RocketMqService rocketMqService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Resource
    TenantService tenantService;
    
    @Override
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        HardwareFailureWarnMsg hardwareFailureWarnMsg = JsonUtil.fromJson(receiverMessage.getOriginContent(), HardwareFailureWarnMsg.class);
        if (Objects.isNull(hardwareFailureWarnMsg) || ObjectUtils.isEmpty(hardwareFailureWarnMsg.getAlarmList())) {
            log.error("PARSE HARDWARE FAILURE WARN MSG ERROR! sessionId={}", receiverMessage.getSessionId());
            return;
        }
        
        List<HardwareFailureWarnMqMsg> list = convertMqMsg(hardwareFailureWarnMsg, electricityCabinet);
        rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_FAILURE_WARNING_BREAKDOWN, JsonUtil.toJson(list));
        
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("sessionId", receiverMessage.getSessionId());
        dataMap.put("msgType", CommonConstant.MSG_TYPE);
        dataMap.put("devId", hardwareFailureWarnMsg.getDevId());
        dataMap.put("t", hardwareFailureWarnMsg.getT());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(receiverMessage.getSessionId()).productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName()).data(dataMap).command(ElectricityIotConstant.HARDWARE_FAILURE_WARN_MSG_ACK).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("HARDWARE WARN MSG ERROR! send command error! requestId:{}", receiverMessage.getSessionId());
        }
    }
    
    private List<HardwareFailureWarnMqMsg> convertMqMsg(HardwareFailureWarnMsg hardwareFailureWarnMsg, ElectricityCabinet electricityCabinet) {
        Tenant tenant = tenantService.queryByIdFromCache(electricityCabinet.getTenantId());
        String tenantName = tenant != null ? tenant.getName() : "";
        
        List<HardwareFailureWarnMqMsg> list = new ArrayList<>();
        hardwareFailureWarnMsg.getAlarmList().forEach(item -> {
            HardwareFailureWarnMqMsg msg = new HardwareFailureWarnMqMsg();
            BeanUtils.copyProperties(item, msg);
            msg.setCabinetId(electricityCabinet.getId());
            msg.setTenantId(electricityCabinet.getTenantId());
            msg.setAddress(electricityCabinet.getAddress());
            msg.setMsgType(hardwareFailureWarnMsg.getMsgType());
            msg.setSignalId(item.getId());
            msg.setCellNo(item.getBoxId());
            msg.setSn(electricityCabinet.getSn());
            if (StringUtils.isNotEmpty(item.getBatterySn())) {
                msg.setSn(item.getBatterySn());
            }
            msg.setCabinetSn(electricityCabinet.getSn());
            msg.setBatterySn(item.getBatterySn());
            msg.setDevId(hardwareFailureWarnMsg.getDevId());
            msg.setReportTime(hardwareFailureWarnMsg.getT());
            msg.setTxnNo(hardwareFailureWarnMsg.getTxnNo());
            msg.setTenantName(tenantName);
            msg.setCabinetName(electricityCabinet.getName());
            list.add(msg);
        });
        return list;
    }
}

@Data
class HardwareFailureWarnMsg {
    
    /**
     * 报文类型：410
     */
    private Integer msgType;
    
    /**
     * 设备Id
     */
    private String devId;
    
    /**
     * 上报时间
     */
    private Long t;
    
    /**
     * 流水号
     */
    private String txnNo;
    
    /**
     * 警告信号量
     */
    private List<HardwareAlarmMsg> alarmList;
}


@Data
class HardwareAlarmMsg {
    
    /**
     * 信号量
     */
    private String id;
    
    /**
     * 告警时间
     */
    private Long alarmTime;
    
    /**
     * 告警事件描述
     */
    private String alarmDesc;
    
    /**
     * 告警标识
     */
    private Integer alarmFlag;
    
    /**
     * 告警消息Id
     */
    private String alarmId;
    
    /**
     * 格挡号
     */
    private Integer boxId;
    
    /**
     * 电池sn
     */
    private String batterySn;
    
    /**
     * 类型：0-告警，1-故障
     */
    private Integer type;
    
    /**
     * 故障发生次数
     */
    private Integer occurNum;
}


@Data
class HardwareFailureWarnMqMsg {
    
    /**
     * 换电柜Id
     */
    private Integer cabinetId;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 设备sn
     */
    private String sn;
    
    /**
     * 柜机sn
     */
    private String cabinetSn;
    
    /**
     * 换电柜地址
     */
    private String address;
    
    /**
     * 报文类型：410
     */
    private Integer msgType;
    
    /**
     * 设备Id
     */
    private String devId;
    
    /**
     * 上报时间
     */
    private Long reportTime;
    
    /**
     * 流水号
     */
    private String txnNo;
    
    /**
     * 信号量
     */
    private String signalId;
    
    /**
     * 告警时间
     */
    private Long alarmTime;
    
    /**
     * 告警事件描述
     */
    private String alarmDesc;
    
    /**
     * 告警标识
     */
    private Integer alarmFlag;
    
    /**
     * 告警消息Id
     */
    private String alarmId;
    
    /**
     * 格挡号
     */
    private Integer cellNo;
    
    /**
     * 电池sn
     */
    private String batterySn;
    
    /**
     * 类型：0-告警，1-故障
     */
    private Integer type;
    
    /**
     * 故障发生次数
     */
    private Integer occurNum;
}