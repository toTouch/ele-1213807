package com.xiliulou.electricity.handler.eleapi.impl;

import com.xiliulou.electricity.constant.EleApiConstant;
import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.service.ApiExchangeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : eclair
 * @date : 2021/11/9 9:31 上午
 */
@Service(value = EleApiConstant.RETURN_ORDER_QUERY)
@Slf4j
public class EleReturnOrderQueryHandler implements EleApiHandler {
    @Autowired
    ApiExchangeOrderService apiExchangeOrderService;

    @Override
    public Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery) {

        return null;
    }
}
