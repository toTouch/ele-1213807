package com.xiliulou.electricity.handler.eleapi.impl;

import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 14:04
 * @Description:
 */
public class EleApiHandlerManager {

	@Autowired
	private Map<String, EleApiHandler> apiHandlerMap;

	public EleApiHandler getInstance(String command) {
		return apiHandlerMap.get(command);
	}
}
