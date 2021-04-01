package com.xiliulou.electricity.service;
import com.xiliulou.electricity.entity.EleWarnRequest;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author: lxc
 * @Date: 2021/3/29 14:08
 * @Description:
 */
public interface EleWarnService {
	Pair<Boolean,Integer> handleEleWarn(EleWarnRequest eleWarnRequest, ElectricityCabinet electricityCabinet);
}
