package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Miss.Li
 * @Date: 2021/8/11 10:24
 * @Description:
 */
@RestController
public class XllThreadPoolExecutorsController {


	//
	@GetMapping(value = "/admin/getAllRunningExecutorServices")
	public R getAllRunningExecutorServices() {
		return R.ok(XllThreadPoolExecutors.getAllRunningExecutorServices());
	}


	//
	@GetMapping(value = "/admin/getRunningExe")
	public R getRunningExe() {
		return R.ok(XllThreadPoolExecutors.getRunningExe());
	}


	//
	@GetMapping(value = "/admin/getRunningExeByName")
	public R getRunningExeByName(@RequestParam("name") String name) {
		return R.ok(XllThreadPoolExecutors.getRunningExeByName(name));
	}

}
