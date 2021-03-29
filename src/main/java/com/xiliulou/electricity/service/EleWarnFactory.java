package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleWarnMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: lxc
 * @Date: 2021/3/29 14:08
 * @Description:
 */
@Service
public class EleWarnFactory {
	@Autowired
	private Map<String, EleWarnService> map = new HashMap<>();

	public static final Map<Integer, String> ELE_WARN_TYPE = new HashMap<>();
	public static final String BATTERY_TAKE_EXCEPTION = "batteryTakeException";
	static {
		ELE_WARN_TYPE.put(EleWarnMsg.TYPE_BATTERY_TAKE_EXCEPTION,BATTERY_TAKE_EXCEPTION);
	}

	public EleWarnService getInstance(Integer type) {
		return map.get(ELE_WARN_TYPE.get(type));
	}


}
