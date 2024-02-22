package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 电池套餐商户返利结算
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-21-10:34
 */
@Component
@Slf4j
@JobHandler(value = "settleBatteryMemberCardRebateRecord")
public class SettleBatteryMemberCardRebateRecord extends IJobHandler {
    
    @Autowired
    private RebateRecordService rebateRecordService;
    
    @Override
    public ReturnT<String> execute(String s) throws Exception {
    
        try {
            rebateRecordService.settleRebateRecordTask();
        } catch (Exception e) {
            log.error("SETTLE REBATE RECORD TASK ERROR", e);
        }
        return IJobHandler.SUCCESS;
    }
}
