package com.xiliulou.electricity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: eclair
 * @Date: 2020/11/23 17:49
 * @Description:
 */
@RestController
public class TestController {
	@GetMapping("/test/hello")
	public String getSTrintg() {
		return "nihao";
	}
}
