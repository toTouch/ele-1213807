package com.xiliulou.electricity.service;

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

	public EleWarnService getInstance(String name) {
		return map.get(name);
	}


}
