package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.TimeUnit;

/**
 * @author: Miss.Li
 * @Date: 2021/8/11 10:24
 * @Description:
 */
@RestController
@Slf4j
public class XllThreadPoolExecutorsController {


	//线程池总数
	@GetMapping(value = "/admin/getAllRunningExecutorServices")
	public R getAllRunningExecutorServices() {
		return R.ok(XllThreadPoolExecutors.getAllRunningExecutorServices());
	}


	//线程池详情
	@GetMapping(value = "/admin/getRunningExe")
	public R getRunningExe(@RequestParam(value = "name", required = false) String name) {
		return R.ok(XllThreadPoolExecutors.getRunningExe(name));
	}



	//终止所有线程池
	@PostMapping(value = "/admin/shutdownAllExe")
	public R shutdownAllExe() {
		XllThreadPoolExecutors.shutdownAllExe();
		return R.ok();
	}


	//终止某个线程池
	@PostMapping(value = "/admin/shutdownExeAndWait")
	public R shutdownExeAndWait(@RequestParam("name") String name,@RequestParam("time") Long time) {
		try {
			XllThreadPoolExecutors.shutdownExeAndWait(name,time, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("删除线程异常",e);
			return R.fail("删除线程出错");
		}
		return R.ok();
	}




}
