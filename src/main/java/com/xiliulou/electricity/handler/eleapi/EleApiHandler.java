package com.xiliulou.electricity.handler.eleapi;

import com.xiliulou.electricity.query.api.ApiRequestQuery;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 13:47
 * @Description:
 */
public interface EleApiHandler {

	Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery);

}
