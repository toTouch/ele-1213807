package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: eclair
 * @Date: 2020/12/14 17:42
 * @Description:
 */
@RestController
public class JonsOuterTestController {
	@Autowired
	UserService userService;

	@GetMapping("/outer/test")
	public R test(@RequestParam("uid") Long uid, @RequestParam("type") Integer type) {
		System.out.println("start===current:" + Thread.currentThread().getName() + ",time:" + System.currentTimeMillis());
		R test = userService.test(uid, type);
		System.out.println("end====current:" + Thread.currentThread().getName() + ",time:" + System.currentTimeMillis());
		return test;
	}
}
