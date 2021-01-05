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
import java.util.concurrent.TimeUnit;


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
	ElectricityCabinetBoxService electricityCabinetBoxService;
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
		//仓门上报
		if(Objects.equals(receiverMessage.getType(), HardwareCommand.ELE_COMMAND_CELL_REPORT_INFO)){
			ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
			if (Objects.isNull(electricityCabinet)) {
				log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
				return false;
			}
			EleCellVo eleCellVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleCellVo.class);
			if (Objects.isNull(eleCellVo)) {
				log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
				return true;
			}
			String cellNo=eleCellVo.getCell_no();
			if (Objects.isNull(cellNo)) {
				log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
				return true;
			}
			ElectricityCabinetBox electricityCabinetBox=new ElectricityCabinetBox();
			electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
			electricityCabinetBox.setCellNo(cellNo);
			//TODO 仓门上报详细信息
			String isLock=eleCellVo.getIs_lock();
			if (Objects.nonNull(isLock)) {
				electricityCabinetBox.setIsLock(Integer.valueOf(isLock));
			}
			String isFan=eleCellVo.getIs_fan();
			if (Objects.nonNull(isFan)) {
				electricityCabinetBox.setIsFan(Integer.valueOf(isFan));
			}
			String temperature=eleCellVo.getTemperature();
			if (Objects.nonNull(temperature)) {
				electricityCabinetBox.setTemperature(temperature);
			}
			String isHeat=eleCellVo.getIs_heat();
			if (Objects.nonNull(isHeat)) {
				electricityCabinetBox.setIsHeat(Integer.valueOf(isHeat));
			}
			String isLight=eleCellVo.getIs_light();
			if (Objects.nonNull(isLight)) {
				electricityCabinetBox.setIsLight(Integer.valueOf(isLight));
			}
			String isForbidden=eleCellVo.getIs_forbidden();
			if (Objects.nonNull(isForbidden)) {
				electricityCabinetBox.setUsableStatus(Integer.valueOf(isForbidden));
			}
			String batteryStatus=eleCellVo.getBatteryStatus();
			if (Objects.nonNull(batteryStatus)) {
				electricityCabinetBox.setStatus(Integer.valueOf(batteryStatus));
			}
			String batteryName=eleCellVo.getBatteryName();
			if (Objects.nonNull(batteryName)) {
				ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
				if(Objects.nonNull(electricityBattery)) {
					electricityCabinetBox.setElectricityBatteryId(electricityBattery.getId());
					//电池所属仓门修改
					ElectricityBattery newElectricityBattery=new ElectricityBattery();
					newElectricityBattery.setId(electricityBattery.getId());
					newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
					newElectricityBattery.setCabinetId(electricityCabinet.getId());
					newElectricityBattery.setUpdateTime(System.currentTimeMillis());
					electricityBatteryService.update(newElectricityBattery);
				}

			}
			electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
		}

		//电池上报
		if(Objects.equals(receiverMessage.getType(),HardwareCommand.ELE_COMMAND_CELL_BATTERY_REPORT_INFO)){
			EleBatteryVo eleBatteryVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVo.class);
			if (Objects.isNull(eleBatteryVo)) {
				log.error("ele battery error! no eleCellVo,{}", receiverMessage.getOriginContent());
				return true;
			}
			String serialNumber=eleBatteryVo.getSerial_number();
			if (Objects.isNull(serialNumber)) {
				log.error("ele battery error! no eleBatteryVo,{}", receiverMessage.getOriginContent());
				return true;
			}
			ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(serialNumber);
			ElectricityBattery newElectricityBattery=new ElectricityBattery();
			newElectricityBattery.setId(electricityBattery.getId());
			newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
			newElectricityBattery.setUpdateTime(System.currentTimeMillis());
			//TODO 电池上报详细信息
			String power=eleBatteryVo.getPower();
			if (Objects.nonNull(power)) {
				newElectricityBattery.setCapacity(Integer.valueOf(power));
			}
			String health=eleBatteryVo.getHealth();
			if (Objects.nonNull(health)) {
				newElectricityBattery.setHealthStatus(Integer.valueOf(health));
			}
			electricityBatteryService.update(newElectricityBattery);
		}
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
