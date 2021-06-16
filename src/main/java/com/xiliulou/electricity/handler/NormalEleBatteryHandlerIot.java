package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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
	@Autowired
	ElectricityCabinetBoxService electricityCabinetBoxService;
	@Autowired
	RedisService redisService;

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
		ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
		if (Objects.isNull(electricityCabinet)) {
			log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
			return false;
		}
		EleBatteryVo eleBatteryVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVo.class);
		if (Objects.isNull(eleBatteryVo)) {
			log.error("ele battery error! no eleCellVo,{}", receiverMessage.getOriginContent());
			return false;
		}
		String cellNo = eleBatteryVo.getCellNo();
		if (StringUtils.isEmpty(cellNo)) {
			log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
			return false;
		}
		ElectricityCabinetBox oldElectricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNo);
		ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
		//若上报时间小于上次上报时间则忽略此条上报
		Long reportTime = eleBatteryVo.getReportTime();
		if (Objects.nonNull(reportTime) && Objects.nonNull(oldElectricityCabinetBox.getReportTime())
				&& oldElectricityCabinetBox.getReportTime() >= reportTime) {
			return false;
		}
		if (Objects.nonNull(reportTime)) {
			electricityCabinetBox.setReportTime(reportTime);
		}
		String batteryName = eleBatteryVo.getBatteryName();
		Boolean existsBattery = eleBatteryVo.getExistsBattery();

		//存在电池但是电池名字没有上报
		if (Objects.nonNull(existsBattery) && StringUtils.isEmpty(batteryName) && existsBattery) {
			return false;
		}

		//缓存存换电柜中电量最多的电池
		BigEleBatteryVo bigEleBatteryVo = redisService.getWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY+electricityCabinet.getId().toString(), BigEleBatteryVo.class);

		//不存在电池
		if (Objects.nonNull(existsBattery) && !existsBattery) {
			batteryName = null;
		}
		if (StringUtils.isEmpty(batteryName)) {
			electricityCabinetBox.setElectricityBatteryId(-1L);
			electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
			electricityCabinetBox.setCellNo(cellNo);
			electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
			electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
			//原来仓门是否有电池
			if (Objects.equals(oldElectricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY)
					&& Objects.nonNull(oldElectricityCabinetBox.getElectricityBatteryId())) {
				//修改电池
				ElectricityBattery newElectricityBattery = new ElectricityBattery();
				newElectricityBattery.setId(oldElectricityCabinetBox.getElectricityBatteryId());
				newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_STATUS);
				electricityBatteryService.updateReport(newElectricityBattery);
			}

			//若最大电池的仓门现在为空，则删除最大电量的缓存
			if (Objects.nonNull(bigEleBatteryVo)) {
				if (Objects.equals(bigEleBatteryVo.getCellNo(), cellNo)) {
					redisService.delete(electricityCabinet.getId().toString());
				}
			}
			return true;
		}
		ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
		if (Objects.isNull(electricityBattery)) {
			log.error("ele battery error! no electricityBattery,sn,{}", batteryName);
			return false;
		}

		//修改电池
		ElectricityBattery newElectricityBattery = new ElectricityBattery();
		newElectricityBattery.setId(electricityBattery.getId());
		newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
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
		electricityBatteryService.updateReport(newElectricityBattery);

		//比较最大电量，保证仓门电池是最大电量的电池
		Double nowPower = eleBatteryVo.getPower();
		BigEleBatteryVo newBigEleBatteryVo = new BigEleBatteryVo();
		newBigEleBatteryVo.setCellNo(cellNo);
		if (Objects.isNull(bigEleBatteryVo)) {
			newBigEleBatteryVo.setPower(nowPower);
			redisService.saveWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY+electricityCabinet.getId().toString(), newBigEleBatteryVo);
		} else {
			Double oldPower = bigEleBatteryVo.getPower();
			if (Objects.nonNull(oldPower) && Objects.nonNull(nowPower) && nowPower > oldPower) {
				newBigEleBatteryVo.setPower(nowPower);
				redisService.saveWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY+electricityCabinet.getId().toString(), newBigEleBatteryVo);
			}
		}

		//修改仓门
		electricityCabinetBox.setElectricityBatteryId(newElectricityBattery.getId());
		electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
		electricityCabinetBox.setCellNo(cellNo);
		electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
		electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);
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
	//reportTime
	private Long reportTime;

	private Boolean existsBattery;

}

