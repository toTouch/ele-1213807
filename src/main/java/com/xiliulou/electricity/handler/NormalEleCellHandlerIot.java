package com.xiliulou.electricity.handler;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
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
public class NormalEleCellHandlerIot extends AbstractIotMessageHandler {
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	RedisService redisService;
	@Autowired
	EleOperateQueueHandler eleOperateQueueHandler;
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
		//仓门上报 TODO 类型
		if(Objects.equals(receiverMessage.getType(),1)){
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
				log.error("ele cell error! no eleBoxRo,{}", receiverMessage.getOriginContent());
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
			electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
		}

		//电池上报 TODO
		if(Objects.equals(receiverMessage.getType(),1)){
			EleBatteryVo eleBatteryVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVo.class);
			if (Objects.isNull(eleBatteryVo)) {
				log.error("ele battery error! no eleCellVo,{}", receiverMessage.getOriginContent());
				return true;
			}
			String cellNo=eleBatteryVo.getCell_no();
			if (Objects.isNull(cellNo)) {
				log.error("ele battery error! no eleBoxRo,{}", receiverMessage.getOriginContent());
				return true;
			}
			//修改电池
			ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(cellNo);
			ElectricityBattery newElectricityBattery=new ElectricityBattery();
			newElectricityBattery.setId(electricityBattery.getId());
			newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
			newElectricityBattery.setUpdateTime(System.currentTimeMillis());
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
}

@Data
class EleBatteryVo {
	private String cell_no;
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
