package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: Miss.Li
 * @Date: 2021/8/11 10:24
 * @Description:
 */
public class XllThreadPoolExecutorsController {


	//
	@GetMapping(value = "/admin/getRunningExe")
	public R getRunningExe() {
		return R.ok(XllThreadPoolExecutors.getRunningExe());
	}

	//
	@GetMapping(value = "/admin/getAllRunningExecutorServices")
	public R getAllRunningExecutorServices() {
		return R.ok(XllThreadPoolExecutors.getAllRunningExecutorServices());
	}


}
