package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.ApiRequestQuery;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 10:19
 * @Description:
 */
public interface ApiService {

	Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery);
}
