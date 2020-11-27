package com.xiliulou.electricity.controller;

import com.xiliulou.electricity.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: eclair
 * @Date: 2020/11/23 17:49
 * @Description:
 */
@RestController
public class TestController {
	@Autowired
	TestService testService;

	@GetMapping("/test/hello")
	public String getSTrintg() {
		return "nihao";
	}

	@GetMapping("/test/lock")
	public String name(@RequestParam("test") String test) {
		System.out.println(test);
		testService.test(test);
		return "nihao";
	}
}
