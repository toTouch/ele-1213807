package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.dto.EleOpenDTO.EleOpenDTOBuilder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
import com.xiliulou.electricity.vo.WarnMsgVo;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_ELE_ORDER_HANDLER)
@Slf4j
public class NormalEleOrderHandlerIot extends AbstractElectricityIotHandler {
	@Autowired
	RedisService redisService;
	@Autowired
	EleOperateQueueHandler eleOperateQueueHandler;

	@Override
	public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
		String sessionId = receiverMessage.getSessionId();
		if (StrUtil.isEmpty(sessionId)) {
			log.warn("no sessionId,{}", receiverMessage.getOriginContent());
			return ;
		}
		EleOrderVo eleOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOrderVo.class);
		//幂等加锁
		Boolean result = redisService.setNx(CacheConstant.ELE_RECEIVER_CACHE_KEY + sessionId + eleOrderVo.getOrderStatus() + receiverMessage.getType(), "true", 10 * 1000L, true);
		if (!result) {
			log.warn("sessionId is lock,{}", sessionId);
			return ;
		}

		//操作失败
		if (eleOrderVo.getIsProcessFail()) {

			//检测失败报错
			WarnMsgVo warnMsgVo = new WarnMsgVo();
			warnMsgVo.setIsNeedEndOrder(eleOrderVo.getIsNeedEndOrder());
			warnMsgVo.setMsg(eleOrderVo.getMsg());
			redisService.set(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + eleOrderVo.getOrderId(), JsonUtil.toJson(warnMsgVo), 5L, TimeUnit.MINUTES);
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
				.cellNo(eleOrderVo.getCellNo())
				.batterySn(eleOrderVo.getBatterySn()).build();
		eleOperateQueueHandler.putQueue(eleOpenDTO);
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
		
		/**
		 * 退电空仓仓门
		 */
		private Integer cellNo;
	}
}


