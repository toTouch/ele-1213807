package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.mns.HardwareHandlerManager;
import lombok.extern.slf4j.Slf4j;
import shaded.org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 13:26
 * @Description:
 */
@Service
@Slf4j
public class EleHardwareHandlerManager extends HardwareHandlerManager {
    @Autowired
    NormalEleOrderHandlerIot normalEleOrderHandlerIot;
    @Autowired
    NormalEleBatteryHandlerIot normalEleBatteryHandlerIot;
    @Autowired
    NormalEleCellHandlerIot normalEleCellHandlerIot;
    @Autowired
    NormalEleExchangeHandlerIot normalEleExchangeHandlerIot;
    @Autowired
    NormalEleOperateHandlerIot normalEleOperateHandlerIot;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    RedisService redisService;

    public Pair<Boolean, String> chooseCommandHandlerProcessSend(HardwareCommandQuery hardwareCommandQuery) {
        if (hardwareCommandQuery.getCommand().contains("cell") || hardwareCommandQuery.getCommand().contains("order")
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.EXCHANGE_CABINET)
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_OPERATE)) {
            log.info("hardwareCommandQuery is -->{}",hardwareCommandQuery);
            return normalEleOrderHandlerIot.handleSendHardwareCommand(hardwareCommandQuery);
        } else {
            log.error("command not support handle,command:{}", hardwareCommandQuery.getCommand());
            return Pair.of(false, "");
        }
    }

    @Override
    public boolean chooseCommandHandlerProcessReceiveMessage(ReceiverMessage receiverMessage) {
        log.info("receiverMessage is -->{}",receiverMessage);
        //电柜在线状态
        if (Objects.isNull(receiverMessage.getType())) {
            if (!StrUtil.isNotEmpty(receiverMessage.getStatus())) {
                return false;
            }
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
                return false;
            }
            //在线状态修改
            ElectricityCabinet newElectricityCabinet=new ElectricityCabinet();
            newElectricityCabinet.setId(electricityCabinet.getId());
            Integer status=1;
            if(Objects.equals(receiverMessage.getStatus(),"online")){
                status=0;
            }
            newElectricityCabinet.setOnlineStatus(status);
            newElectricityCabinet.setPowerStatus(status);
            if (electricityCabinetService.update(newElectricityCabinet) > 0) {
                redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + newElectricityCabinet.getId());
                redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey()+electricityCabinet.getDeviceName());
            }
            log.error("type is null,{}",receiverMessage.getOriginContent());
            return false;
        }
        if (receiverMessage.getType().contains("order")) {
            return normalEleOrderHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (receiverMessage.getType().contains("operate")) {
            return normalEleOperateHandlerIot.receiveMessageProcess(receiverMessage);
        }else if (receiverMessage.getType().contains("cell")) {
            return normalEleCellHandlerIot.receiveMessageProcess(receiverMessage);
        }  else if(receiverMessage.getType().contains("battery")){
            return normalEleBatteryHandlerIot.receiveMessageProcess(receiverMessage);
        }else if(Objects.equals(receiverMessage.getType(),HardwareCommand.EXCHANGE_CABINET)){
            return normalEleExchangeHandlerIot.receiveMessageProcess(receiverMessage);
        } else {
            log.error("command not support handle,command:{}", receiverMessage.getType());
            return false;
        }
    }

}