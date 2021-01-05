package com.xiliulou.electricity.handler;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.HardwareCommand;
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
import shaded.org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;


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
		EleBatteryVo eleBatteryVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVo.class);
		if (Objects.isNull(eleBatteryVo)) {
			log.error("ele battery error! no eleCellVo,{}", receiverMessage.getOriginContent());
			return true;
		}
		String serialNumber = eleBatteryVo.getSerial_number();
		if (Objects.isNull(serialNumber)) {
			log.error("ele battery error! no eleBatteryVo,{}", receiverMessage.getOriginContent());
			return true;
		}
		ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(serialNumber);
		ElectricityBattery newElectricityBattery = new ElectricityBattery();
		newElectricityBattery.setId(electricityBattery.getId());
		newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
		newElectricityBattery.setUpdateTime(System.currentTimeMillis());
		//TODO 电池上报详细信息
		String power = eleBatteryVo.getPower();
		if (Objects.nonNull(power)) {
			newElectricityBattery.setCapacity(Integer.valueOf(power));
		}
		String health = eleBatteryVo.getHealth();
		if (Objects.nonNull(health)) {
			newElectricityBattery.setHealthStatus(Integer.valueOf(health));
		}
		electricityBatteryService.update(newElectricityBattery);
		return true;
	}

}


@Data
class EleBatteryVo {
	private String serial_number;
	//电压
	private String voltage;
	//电芯数量
	private String batteries_count;
	//电量
	private String power;
	//容量
	private String capacity;
	//输出电流
	private String output_current;
	//输出电流
	private String recharging_current;
	//温度
	private String temperature;
	//健康状态
	private String health;
}
