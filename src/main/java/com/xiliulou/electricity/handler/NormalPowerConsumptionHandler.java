package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.service.ElectricityCabinetPowerService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalPowerConsumptionHandler extends AbstractIotMessageHandler {
	@Autowired
	RedisService redisService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	ElectricityCabinetPowerService electricityCabinetPowerService;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
		String sessionId = receiverMessage.getSessionId();
		if (StrUtil.isEmpty(sessionId)) {
			log.error("no sessionId,{}", receiverMessage.getOriginContent());
			return false;
		}

		PowerConsumption powerConsumption = JsonUtil.fromJson(receiverMessage.getOriginContent(), PowerConsumption.class);
		if (Objects.isNull(powerConsumption)) {
			log.error("NORMAL POWER ERROR! parse  power error! originContent={}", receiverMessage.getOriginContent());
			return false;
		}

		if (!powerConsumption.isSuccess()) {
			log.warn("NORMAL POWER WARN! power consumption warn! originContent={}", receiverMessage.getOriginContent());
			return false;
		}

		ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
		if (Objects.isNull(electricityCabinet)) {
			log.error("NORMAL POWER ERROR! no electricityCabinet! p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
			return false;
		}

		ElectricityCabinetPower build = ElectricityCabinetPower.builder()
				.sumPower(Double.parseDouble(powerConsumption.getSumConsumption()))
				.sameDayPower(Double.parseDouble(powerConsumption.getPowerConsumption()))
				.updateTime(System.currentTimeMillis())
				.createTime(System.currentTimeMillis())
				.date(LocalDate.parse(powerConsumption.getDate(), formatter))
				.eid(electricityCabinet.getId().longValue())
				.build();
		electricityCabinetPowerService.insertOrUpdate(build);
		return true;
	}

}

@Data
class PowerConsumption {
	private String powerConsumption;
	private String date;
	private String sumConsumption;
	private boolean success;
}
