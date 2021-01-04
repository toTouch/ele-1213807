package com.xiliulou.electricity.handler;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
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
	NormalEleOperHandlerIot normalEleOperHandlerIot;
	@Autowired
	NormalEleCellHandlerIot normalEleCellHandlerIot;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	RedisService redisService;

	public Pair<Boolean, String> chooseCommandHandlerProcessSend(HardwareCommandQuery hardwareCommandQuery) {
		if (hardwareCommandQuery.getCommand().contains("cell")) {
			return normalEleOperHandlerIot.handleSendHardwareCommand(hardwareCommandQuery);
		} else {
			log.error("command not support handle,command:{}", hardwareCommandQuery.getCommand());
			return Pair.of(false, "");
		}
	}

	@Override
	public boolean chooseCommandHandlerProcessReceiveMessage(ReceiverMessage receiverMessage) {
		//幂等加锁
		Boolean result=redisService.setNx(ElectricityCabinetConstant.ELE_RECEIVER_CACHE_KEY + receiverMessage.getSessionId(), "true", 10*1000L,true);
		if(!result){
			return false;
		}
		//这种情况不会出现
		if (Objects.isNull(receiverMessage.getType())) {
			return false;
		}
		if (receiverMessage.getType().contains("oper")) {
			return normalEleOperHandlerIot.receiveMessageProcess(receiverMessage);
		} else if (receiverMessage.getType().contains("cell")) {
			return normalEleCellHandlerIot.receiveMessageProcess(receiverMessage);
		} else {
			log.error("command not support handle,command:{}", receiverMessage.getType());
			return false;
		}
	}

}