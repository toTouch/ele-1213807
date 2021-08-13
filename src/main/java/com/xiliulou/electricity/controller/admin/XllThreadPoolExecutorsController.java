package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.vo.XllThreadPoolExecutorServiceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: Miss.Li
 * @Date: 2021/8/11 10:24
 * @Description:
 */
@RestController
@Slf4j
public class XllThreadPoolExecutorsController {


	//
	@GetMapping(value = "/admin/getAllRunningExecutorServices")
	public R getAllRunningExecutorServices() {
		return R.ok(XllThreadPoolExecutors.getAllRunningExecutorServices());
	}


	//
	@GetMapping(value = "/admin/getRunningExe")
	public R getRunningExe(@RequestParam(value = "name", required = false) String name) {
		Map<String, XllThreadPoolExecutorService> result= XllThreadPoolExecutors.getRunningExe(name);
		Map<String, XllThreadPoolExecutorServiceVO> map=new HashMap<>();
		for(String key:result.keySet()){//keySet获取map集合key的集合  然后在遍历key即可
			XllThreadPoolExecutorService xllThreadPoolExecutorService = result.get(key);//
			XllThreadPoolExecutorServiceVO xllThreadPoolExecutorServiceVO=new XllThreadPoolExecutorServiceVO();
			BeanUtil.copyProperties(xllThreadPoolExecutorService,xllThreadPoolExecutorServiceVO);
			xllThreadPoolExecutorServiceVO.setCallerInfo(xllThreadPoolExecutorService.getCallerInfo());
			xllThreadPoolExecutorServiceVO.setQueueSize(xllThreadPoolExecutorService.getQueenSize());
			map.put(key,xllThreadPoolExecutorServiceVO);
		}
		return R.ok(map);
	}



	//
	@PostMapping(value = "/admin/shutdownAllExe")
	public R shutdownAllExe() {
		XllThreadPoolExecutors.shutdownAllExe();
		return R.ok();
	}


	//
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
