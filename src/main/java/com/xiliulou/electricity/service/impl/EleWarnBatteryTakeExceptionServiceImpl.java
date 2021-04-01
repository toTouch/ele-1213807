package com.xiliulou.electricity.service.impl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.EleWarnRequest;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.EleWarnService;
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

	@Override
	public Pair<Boolean, Integer> handleEleWarn(EleWarnRequest eleWarnRequest, ElectricityCabinet electricityCabinet) {
		//锁住换电柜
		redisService.saveWithString(ElectricityCabinetConstant.UNLOCK_CABINET_CACHE+electricityCabinet.getId(),electricityCabinet.getId());
		return Pair.of(true,null);
	}
}
