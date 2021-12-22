package com.xiliulou.electricity.handler.eleapi;

import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 14:04
 * @Description:
 */
@Service
public class EleApiHandlerManager {

	@Autowired
	private Map<String, EleApiHandler> apiHandlerMap;

	public EleApiHandler getInstance(String command) {
		return apiHandlerMap.get(command);
	}
}
