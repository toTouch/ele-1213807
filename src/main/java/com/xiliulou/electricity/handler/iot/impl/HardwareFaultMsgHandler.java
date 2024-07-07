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
@Service(value = ElectricityIotConstant.HARDWARE_FAULT_MSG_HANDLER)
@Slf4j
public class HardwareFaultMsgHandler extends AbstractElectricityIotHandler {
    
    @Autowired
    RocketMqService rocketMqService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Resource
    TenantService tenantService;
    
    @Override
    protected void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        HardwareFaultWarnMsg hardwareFaultWarnMsg = JsonUtil.fromJson(receiverMessage.getOriginContent(), HardwareFaultWarnMsg.class);
        if (Objects.isNull(hardwareFaultWarnMsg) || ObjectUtils.isEmpty(hardwareFaultWarnMsg.getAlarmList())) {
            log.error("PARSE HARDWARE FAULT WARN MSG ERROR! sessionId={}", receiverMessage.getSessionId());
            return;
        }
        
        List<HardwareFaultWarnMqMsg> list = convertMqMsg(hardwareFaultWarnMsg, electricityCabinet);
        rocketMqService.sendAsyncMsg(MqProducerConstant.FAULT_FAILURE_WARNING_BREAKDOWN, JsonUtil.toJson(list));
        
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("sessionId", receiverMessage.getSessionId());
        dataMap.put("msgType", CommonConstant.MSG_TYPE);
        dataMap.put("devId", hardwareFaultWarnMsg.getDevId());
        dataMap.put("t", hardwareFaultWarnMsg.getT());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(receiverMessage.getSessionId()).productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName()).data(dataMap).command(ElectricityIotConstant.HARDWARE_FAULT_WARN_MSG_ACK).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("HARDWARE FAULT WARN MSG ERROR! send command error! requestId:{}", receiverMessage.getSessionId());
        }
    }
    
    private List<HardwareFaultWarnMqMsg> convertMqMsg(HardwareFaultWarnMsg hardwareFaultWarnMsg, ElectricityCabinet electricityCabinet) {
        Tenant tenant = tenantService.queryByIdFromCache(electricityCabinet.getTenantId());
        String tenantName = tenant != null ? tenant.getName() : "";
        
        List<HardwareFaultWarnMqMsg> list = new ArrayList<>();
        hardwareFaultWarnMsg.getAlarmList().forEach(item -> {
            HardwareFaultWarnMqMsg msg = new HardwareFaultWarnMqMsg();
            BeanUtils.copyProperties(item, msg);
            msg.setCabinetId(electricityCabinet.getId());
            msg.setTenantId(electricityCabinet.getTenantId());
            msg.setAddress(electricityCabinet.getAddress());
            msg.setMsgType(hardwareFaultWarnMsg.getMsgType());
            msg.setSignalId(item.getId());
            msg.setCellNo(item.getBoxId());
            msg.setSn(electricityCabinet.getSn());
            if (StringUtils.isNotEmpty(item.getBatterySn())) {
                msg.setSn(item.getBatterySn());
            }
            msg.setCabinetSn(electricityCabinet.getSn());
            msg.setDevId(hardwareFaultWarnMsg.getDevId());
            msg.setReportTime(hardwareFaultWarnMsg.getT());
            msg.setTxnNo(hardwareFaultWarnMsg.getTxnNo());
            msg.setTenantName(tenantName);
            msg.setCabinetName(electricityCabinet.getName());
            list.add(msg);
        });
        return list;
    }
    
    public void testSend(String msg, Integer type) {
        // todo 告警同步测试需要删除
        
        List<HardwareFailureWarnMqMsg> list = new ArrayList<>();
        HardwareFailureWarnMqMsg hardwareFailureWarnMsg = JsonUtil.fromJson(msg, HardwareFailureWarnMqMsg.class);
        long currentTimeMillis = System.currentTimeMillis();
        hardwareFailureWarnMsg.setAlarmTime(currentTimeMillis);
        hardwareFailureWarnMsg.setReportTime(currentTimeMillis);
        list.add(hardwareFailureWarnMsg);
        
        try {
            Thread.sleep(1000);
            log.info("HARDWARE FAULT WARN SEND START MSG={}", JsonUtil.toJson(list));
            rocketMqService.sendAsyncMsg(MqProducerConstant.FAULT_FAILURE_WARNING_BREAKDOWN, JsonUtil.toJson(list));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        
        /*List<HardwareFailureWarnMqMsg> list = new ArrayList<>();
        
        for (int i = 0; i < 1;i++) {
            HardwareFailureWarnMqMsg hardwareFailureWarnMsg = JsonUtil.fromJson(msg, HardwareFailureWarnMqMsg.class);
            long currentTimeMillis = System.currentTimeMillis();
            hardwareFailureWarnMsg.setAlarmTime(currentTimeMillis);
            hardwareFailureWarnMsg.setReportTime(currentTimeMillis);
            hardwareFailureWarnMsg.setAlarmId(String.valueOf(i));
            hardwareFailureWarnMsg.setAlarmFlag(type);
            list.add(hardwareFailureWarnMsg);
        }
        
        log.info("HARDWARE FAILURE WARN SEND START TEST MSG list size={}", list.size());
        
        List<List<HardwareFailureWarnMqMsg>> partition = ListUtils.partition(list, 4);
        log.info("HARDWARE FAILURE WARN SEND START TEST MSG list size={}", partition.size(), partition.get(0));
        partition.forEach(item -> {
            try {
                Thread.sleep(1000);
                log.info("HARDWARE FAULT WARN SEND START MSG={}", JsonUtil.toJson(list));
                rocketMqService.sendAsyncMsg(MqProducerConstant.FAULT_FAILURE_WARNING_BREAKDOWN, JsonUtil.toJson(list));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
        });*/
    }
    
}

@Data
class HardwareFaultWarnMsg {
    
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
    private List<HardwareFaultMsg> alarmList;
}


@Data
class HardwareFaultMsg {
    
    /**
     * 信号量
     */
    private String id;
    
    /**
     * 告警时间
     */
    private Long alarmTime;
    
    /**
     * 告警结束时间
     */
    private Long alarmEndTime;
    
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
class HardwareFaultWarnMqMsg {
    
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
     * 告警结束时间
     */
    private Long alarmEndTime;
    
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
     * 类型：0-告警，1-故障
     */
    private Integer type;
    
    /**
     * 故障发生次数
     */
    private Integer occurNum;
    
    private String cabinetSn;
}