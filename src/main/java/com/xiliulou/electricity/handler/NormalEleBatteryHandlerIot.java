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
public class NormalEleBatteryHandlerIot extends AbstractIotMessageHandler {
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
            EleBatteryVo eleBatteryVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVo.class);
            if (Objects.isNull(eleBatteryVo)) {
                log.error("ele battery error! no eleCellVo,{}", receiverMessage.getOriginContent());
                return;
            }
            String cellNo = eleBatteryVo.getCellNo();
            if (StringUtils.isEmpty(cellNo)) {
                log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
                return;
            }
            String batteryName = eleBatteryVo.getBatteryName();
            if (StringUtils.isEmpty(batteryName)) {
                ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
                electricityCabinetBox.setElectricityBatteryId(-1L);
                electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
                electricityCabinetBox.setCellNo(cellNo);
                electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
                electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
                return;
            }
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
            if (Objects.isNull(electricityBattery)) {
                log.error("ele battery error! no electricityBattery,sn,{}", batteryName);
                return;
            }
            //修改电池
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            Double power = eleBatteryVo.getPower();
            if (Objects.nonNull(power)) {
                newElectricityBattery.setPower(power * 100);
            }
            String health = eleBatteryVo.getHealth();
            if (StringUtils.isNotEmpty(health)) {
                newElectricityBattery.setHealthStatus(Integer.valueOf(health));
            }
            String chargeStatus = eleBatteryVo.getChargeStatus();
            if (StringUtils.isNotEmpty(chargeStatus)) {
                newElectricityBattery.setChargeStatus(Integer.valueOf(chargeStatus));
            }
            electricityBatteryService.update(newElectricityBattery);
            //修改仓门
            ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
            electricityCabinetBox.setElectricityBatteryId(newElectricityBattery.getId());
            electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
            electricityCabinetBox.setCellNo(cellNo);
            electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
            electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
        });
        return true;
    }

}


@Data
class EleBatteryVo {
    private String batteryName;
    //电量
    private Double power;
    //健康状态
    private String health;
    //充电状态
    private String chargeStatus;
    //cellNo
    private String cellNo;
}
