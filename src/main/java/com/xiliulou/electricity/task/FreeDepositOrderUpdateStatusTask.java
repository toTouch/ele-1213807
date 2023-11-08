package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-11-08-20:52
 */
@Component
@Slf4j
@JobHandler(value = "freeDepositOrderUpdateStatusTask")
public class FreeDepositOrderUpdateStatusTask extends IJobHandler{
    
    @Autowired
    private FreeDepositOrderService freeDepositOrderService;
    
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            freeDepositOrderService.freeDepositOrderUpdateStatusTask();
        } catch (Exception e) {
            log.error("xxl-job FreeDepositOrderUpdateStatusTask fail", e);
        }
        return IJobHandler.SUCCESS;
    }
}
