package com.xiliulou.electricity.service.impl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.EleWarnRequest;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.EleWarnService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2021/3/29 14:08
 * @Description:
 */
@Service("batteryTakeException")
public class EleWarnBatteryTakeExceptionServiceImpl implements EleWarnService {

	@Autowired
	RedisService redisService;

	@Autowired
	ElectricityCabinetOrderService electricityCabinetOrderService;

	@Autowired
	UserInfoService userInfoService;

	@Override
	public Pair<Boolean, Integer> handleEleWarn(EleWarnRequest eleWarnRequest, ElectricityCabinet electricityCabinet) {
		//锁住换电柜
		redisService.saveWithString(ElectricityCabinetConstant.UNLOCK_CABINET_CACHE+electricityCabinet.getId(),electricityCabinet.getId());
		//禁用用户导致锁定柜机的用户
		if(Objects.nonNull(eleWarnRequest)&&Objects.nonNull(eleWarnRequest.getCellNo())) {
			ElectricityCabinetOrder electricityCabinetOrder=electricityCabinetOrderService.queryByCellNo(eleWarnRequest.getCellNo());
			if(Objects.nonNull(electricityCabinetOrder)){
				UserInfo userInfo=new UserInfo();
				userInfo.setUid(electricityCabinetOrder.getUid());
				userInfo.setUsableStatus(UserInfo.USER_UN_USABLE_STATUS);
				userInfoService.updateByUid(userInfo);
			}
		}
		return Pair.of(true,null);
	}
}
