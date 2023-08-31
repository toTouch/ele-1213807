package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 套餐过期生成滞纳金订单
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-19-13:55
 */
@Component
@Slf4j
@JobHandler(value = "batteryMembercardExpireServiceFeeOrderTask")
public class BatteryMembercardExpireServiceFeeOrderTask extends IJobHandler {

    @Autowired
    EleBatteryServiceFeeOrderService batteryServiceFeeOrderService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {

        try {
            batteryServiceFeeOrderService.membercardExpireGenerateServiceFeeOrder(s);
        } catch (Exception e) {
            log.error("xxl-job电池套餐过期生成滞纳金订单失败", e);
        }
        return IJobHandler.SUCCESS;

    }
}
