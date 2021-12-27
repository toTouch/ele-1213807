package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.EleOtherConfig;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.EleOtherConfigService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Objects;

/**
 * @Auther: Hardy
 * @Date: 2021/7/30 15:52
 * @Description:
 */
@Service
@Slf4j
public class IcIdCommandIotHandler extends AbstractIotMessageHandler {
	@Autowired
	private ElectricityCabinetService electricityCabinetService;
	@Autowired
	private EleOtherConfigService eleOtherConfigService;

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
	@Transactional(rollbackFor = Exception.class)
	public boolean receiveMessageProcess(ReceiverMessage receiverMessage) {
		ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
		if (Objects.isNull(electricityCabinet)) {
			log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
			return false;
		}

		EleIccidVo eleIccidVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleIccidVo.class);
		String iccid = eleIccidVo.getIccid();
		if (StrUtil.isEmpty(iccid)) {
			log.warn("ELE WAR! no iccid! eleIccidVo={}", eleIccidVo);
			return false;
		}

		EleOtherConfig eleOtherConfig = eleOtherConfigService.queryByEidFromCache(electricityCabinet.getId());
		if(Objects.nonNull(eleOtherConfig)){
			eleOtherConfig.setCardNumber(iccid);
			eleOtherConfig.setUpdateTime(System.currentTimeMillis());

			Integer update = eleOtherConfigService.update(eleOtherConfig);
			if (update > 0){
				return true;
			}
		}else {
			eleOtherConfig = new EleOtherConfig();
			eleOtherConfig.setEId(electricityCabinet.getId());
			eleOtherConfig.setCardNumber(iccid);
			eleOtherConfig.setTenantId(electricityCabinet.getTenantId());
			eleOtherConfig.setCreateTime(System.currentTimeMillis());
			eleOtherConfig.setUpdateTime(System.currentTimeMillis());
			eleOtherConfig.setDelFlag(EleOtherConfig.DEL_NORMAL);

			Integer insert = eleOtherConfigService.insert(eleOtherConfig);
			if (insert > 0){
				return true;
			}
		}


		return false;
	}
}

@Data
class EleIccidVo {
	//iccid
	private String iccid;
}
