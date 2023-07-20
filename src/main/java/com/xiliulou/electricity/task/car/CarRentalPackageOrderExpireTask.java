package com.xiliulou.electricity.task.car;

import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Component;

import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 租车套餐购买订单过期 Job
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@JobHandler
public class CarRentalPackageOrderExpireTask extends IJobHandler {

    /**
     * 车辆套餐购买订单过期任务
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        return null;
    }
}
