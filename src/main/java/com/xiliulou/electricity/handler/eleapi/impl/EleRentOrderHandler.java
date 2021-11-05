package com.xiliulou.electricity.handler.eleapi.impl;

import com.xiliulou.electricity.constant.EleApiConstant;
import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import com.xiliulou.electricity.query.ApiRequestQuery;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 14:07
 * @Description:
 */

@Service(value = EleApiConstant.RENT_ORDER)
public class EleRentOrderHandler implements EleApiHandler {


	@Override
	public Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery) {
		return null;
	}
}
