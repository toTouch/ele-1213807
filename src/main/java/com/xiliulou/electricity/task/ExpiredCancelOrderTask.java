package com.xiliulou.electricity.task;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName : Expired
 * @Description : 每分钟设置未支付订单为已取消
 * @Author : YG
 * @Date: 2020-06-19 17:18
 */
@Component
@JobHandler(value = "expiredCancelOrderTask")
@Slf4j
public class ExpiredCancelOrderTask extends IJobHandler {

	//处理未支付寄存订单 (每分钟执行一次)
	@Override
	public ReturnT<String> execute(String s) throws Exception {
		log.error("xll_job 搭建成功");
		return IJobHandler.SUCCESS;
	}
}
