package com.xiliulou.electricity.handler;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.dto.EleOpenDTO.EleOpenDTOBuilder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import shaded.org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalEleOrderHandlerIot extends AbstractIotMessageHandler {
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	RedisService redisService;
	@Autowired
	EleOperateQueueHandler eleOperateQueueHandler;
	@Autowired
	ElectricityCabinetBoxService electricityCabinetBoxService;

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
		//幂等加锁
		Boolean result=redisService.setNx(ElectricityCabinetConstant.ELE_RECEIVER_CACHE_KEY + sessionId, "true", 10*1000L,true);
		if(!result){
			log.error("sessionId is lock,{}", sessionId);
			return false;
		}
		EleOpenDTOBuilder builder = EleOpenDTO.builder();

		//操作回调的放在redis中
		if (Objects.nonNull(receiverMessage.getSuccess()) && "True".equalsIgnoreCase(receiverMessage.getSuccess())) {
			redisService.set(ElectricityCabinetConstant.ELE_OPERATOR_CACHE_KEY + sessionId, "true", 60L, TimeUnit.SECONDS);
			builder.operResult(true);
		} else {
			redisService.set(ElectricityCabinetConstant.ELE_OPERATOR_CACHE_KEY + sessionId, "false", 60L, TimeUnit.SECONDS);
			builder.operResult(false);
		}

		if (sessionId.contains(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX)) {
			EleOpenDTO eleOpenDTO = builder
					.sessionId(sessionId)
					.originContent(receiverMessage.getOriginContent())
					.type(receiverMessage.getType()).build();
			eleOperateQueueHandler.putQueue(eleOpenDTO);
		}
		return true;
	}
}
