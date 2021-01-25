package com.xiliulou.electricity.handler;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;
import shaded.org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalEleCellHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    protected Pair<SendHardwareMessage, String> generateMsg(HardwareCommandQuery hardwareCommandQuery) {
        String sessionId = generateSessionId(hardwareCommandQuery);
        SendHardwareMessage message = SendHardwareMessage.builder()
                .sessionId(sessionId)
                .type(hardwareCommandQuery.getCommand())
                .data(hardwareCommandQuery.getData()).build();
        return Pair.of(message, sessionId);
    }

    @Override
    protected boolean receiveMessageProcess(ReceiverMessage receiverMessage) {
        executorService.execute(() -> {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
                return;
            }
            EleCellVo eleCellVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleCellVo.class);
            if (Objects.isNull(eleCellVo)) {
                log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
                return;
            }
            String cellNo = eleCellVo.getCell_no();
            if (StringUtils.isEmpty(cellNo)) {
                log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
                return;
            }
            Long batteryId = null;
            String batteryName = eleCellVo.getBatteryName();
            if (StringUtils.isEmpty(batteryName)) {
                ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
                if (Objects.isNull(electricityBattery)) {
                    log.error("ele battery error! no electricityBattery,sn,{}", batteryName);
                    return;
                }
                batteryId = electricityBattery.getId();
            }
            Long finalBatteryId = batteryId;
            ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
            electricityCabinetBox.setElectricityBatteryId(finalBatteryId);
            electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
            electricityCabinetBox.setCellNo(cellNo);
            String isLock = eleCellVo.getIs_lock();
            if (StringUtils.isNotEmpty(isLock)) {
                electricityCabinetBox.setIsLock(Integer.valueOf(isLock));
            }
            String isFan = eleCellVo.getIs_fan();
            if (StringUtils.isNotEmpty(isFan)) {
                electricityCabinetBox.setIsFan(Integer.valueOf(isFan));
            }
            String temperature = eleCellVo.getTemperature();
            if (StringUtils.isNotEmpty(temperature)) {
                electricityCabinetBox.setTemperature(temperature);
            }
            String isHeat = eleCellVo.getIs_heat();
            if (StringUtils.isNotEmpty(isHeat)) {
                electricityCabinetBox.setIsHeat(Integer.valueOf(isHeat));
            }
            String isLight = eleCellVo.getIs_light();
            if (StringUtils.isNotEmpty(isLight)) {
                electricityCabinetBox.setIsLight(Integer.valueOf(isLight));
            }
            String isForbidden = eleCellVo.getIs_forbidden();
            if (StringUtils.isNotEmpty(isForbidden)) {
                electricityCabinetBox.setUsableStatus(Integer.valueOf(isForbidden));
            }
            String batteryStatus = eleCellVo.getBatteryStatus();
            if (StringUtils.isNotEmpty(batteryStatus)) {
                electricityCabinetBox.setStatus(Integer.valueOf(batteryStatus));
            }
            electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
        });

        return true;
    }
}

@Data
class EleCellVo {
    //仓门号
    private String cell_no;
    //门锁状态
    private String is_lock;
    //风扇状态
    private String is_fan;
    //温度
    private String temperature;
    //加热状态
    private String is_heat;
    //指示灯状态
    private String is_light;
    //可用禁用
    private String is_forbidden;
    //可用禁用
    private String batteryStatus;
    //电池编号
    private String batteryName;
}
