package com.xiliulou.electricity.task.car;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        // 1. 查询会员套餐表中，套餐购买订单已过期的数据（不限制，时间到 或者 限制）
        log.info("CarRentalPackageOrderExpireTask begin.");
        try {
        } catch (Exception e) {
            log.info("CarRentalPackageOrderExpireTask error. ", e);
        }
        log.info("CarRentalPackageOrderExpireTask end.");

        return IJobHandler.SUCCESS;
    }
}
