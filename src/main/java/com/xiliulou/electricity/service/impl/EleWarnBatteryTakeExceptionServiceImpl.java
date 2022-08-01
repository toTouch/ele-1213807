package com.xiliulou.electricity.service.impl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.EleWarnRequest;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.EleWarnService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		return Pair.of(true,null);
	}
}
