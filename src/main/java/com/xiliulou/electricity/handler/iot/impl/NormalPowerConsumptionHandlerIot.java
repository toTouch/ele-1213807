package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetPowerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_POWER_CONSUMPTION_HANDLER)
@Slf4j
public class NormalPowerConsumptionHandlerIot extends AbstractElectricityIotHandler {
	@Autowired
	RedisService redisService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	ElectricityCabinetPowerService electricityCabinetPowerService;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);


	@Override
	public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
		String sessionId = receiverMessage.getSessionId();
		if (StrUtil.isEmpty(sessionId)) {
			log.error("no sessionId,{}", receiverMessage.getOriginContent());
			return ;
		}

		PowerConsumption powerConsumption = JsonUtil.fromJson(receiverMessage.getOriginContent(), PowerConsumption.class);
		if (Objects.isNull(powerConsumption)) {
			log.error("NORMAL POWER ERROR! parse  power error! originContent={}", receiverMessage.getOriginContent());
			return ;
		}

		if (!powerConsumption.isSuccess()) {
			log.warn("NORMAL POWER WARN! power consumption warn! originContent={}", receiverMessage.getOriginContent());
			return ;
		}

		ElectricityCabinetPower build = ElectricityCabinetPower.builder()
				.sumPower(Double.parseDouble(powerConsumption.getSumConsumption()))
				.sameDayPower(Double.parseDouble(powerConsumption.getPowerConsumption()))
				.updateTime(System.currentTimeMillis())
				.createTime(System.currentTimeMillis())
				.date(LocalDate.parse(powerConsumption.getDate(), formatter))
				.eid(electricityCabinet.getId().longValue())
				.tenantId(electricityCabinet.getTenantId())
				.build();
		electricityCabinetPowerService.insertOrUpdate(build);
	}


	@Data
	class PowerConsumption {
		private String powerConsumption;
		private String date;
		private String sumConsumption;
		private boolean success;
	}
}


