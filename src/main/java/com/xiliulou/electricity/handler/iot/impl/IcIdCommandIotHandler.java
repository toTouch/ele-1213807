package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleOtherConfig;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.EleOtherConfigService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @Auther: Hardy
 * @Date: 2021/7/30 15:52
 * @Description:
 */
@Service(value= ElectricityIotConstant.ICID_COMMAND_IOT_HANDLER)
@Slf4j
public class IcIdCommandIotHandler extends AbstractElectricityIotHandler {

	@Autowired
	private EleOtherConfigService eleOtherConfigService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

		EleIccidVo eleIccidVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleIccidVo.class);
		String iccid = eleIccidVo.getIccid();
		if (StrUtil.isEmpty(iccid)) {
			log.warn("ELE WAR! no iccid! eleIccidVo={}", eleIccidVo);
			return ;
		}

		EleOtherConfig eleOtherConfig = eleOtherConfigService.queryByEidFromCache(electricityCabinet.getId());
		if(Objects.nonNull(eleOtherConfig)){
			eleOtherConfig.setCardNumber(iccid);
			eleOtherConfig.setUpdateTime(System.currentTimeMillis());

			eleOtherConfigService.update(eleOtherConfig);
		}else {
			eleOtherConfig = EleOtherConfig.builder()
					.eid(electricityCabinet.getId())
					.cardNumber(iccid)
					.tenantId(electricityCabinet.getTenantId())
					.createTime(System.currentTimeMillis())
					.updateTime(System.currentTimeMillis())
					.delFlag(EleOtherConfig.DEL_NORMAL).build();

			eleOtherConfigService.insert(eleOtherConfig);
		}
	}


	@Data
	class EleIccidVo {
		//iccid
		private String iccid;
	}
}


