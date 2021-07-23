package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.dto.EleOpenDTO.EleOpenDTOBuilder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
import com.xiliulou.electricity.vo.WarnMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.tuple.Pair;
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
	RedisService redisService;
	@Autowired
	EleOperateQueueHandler eleOperateQueueHandler;

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
		EleOrderVo eleOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOrderVo.class);
		//幂等加锁
		Boolean result = redisService.setNx(ElectricityCabinetConstant.ELE_RECEIVER_CACHE_KEY + sessionId + eleOrderVo.getOrderStatus() + receiverMessage.getType(), "true", 10 * 1000L, true);
		if (!result) {
			log.error("sessionId is lock,{}", sessionId);
			return false;
		}


		//操作失败
		if (eleOrderVo.getIsProcessFail()) {

			//检测失败报错
			WarnMsgVo warnMsgVo = new WarnMsgVo();
			warnMsgVo.setIsNeedEndOrder(eleOrderVo.getIsNeedEndOrder());
			warnMsgVo.setMsg(eleOrderVo.getMsg());
			redisService.set(ElectricityCabinetConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + eleOrderVo.getOrderId(), JsonUtil.toJson(warnMsgVo), 5L, TimeUnit.MINUTES);
		}

		EleOpenDTOBuilder builder = EleOpenDTO.builder();

		EleOpenDTO eleOpenDTO = builder
				.sessionId(sessionId)
				.type(receiverMessage.getType())
				.orderStatus(eleOrderVo.getOrderStatus())
				.orderSeq(eleOrderVo.getOrderSeq())
				.orderId(eleOrderVo.getOrderId())
				.isNeedEndOrder(eleOrderVo.getIsNeedEndOrder())
				.isProcessFail(eleOrderVo.getIsProcessFail())
				.msg(eleOrderVo.getMsg())
				.batterySn(eleOrderVo.getBatterySn()).build();
		eleOperateQueueHandler.putQueue(eleOpenDTO);
		return true;
	}
}

@Data
class EleOrderVo {

	//订单Id
	private String orderId;
	//本次操作是否执行失败
	private Boolean isProcessFail;
	//是否需要结束订单
	private Boolean isNeedEndOrder;
	//订单状态序号
	private Double orderSeq;
	//orderStatus
	private String orderStatus;
	//msg
	private String msg;

	private String batterySn;
}
