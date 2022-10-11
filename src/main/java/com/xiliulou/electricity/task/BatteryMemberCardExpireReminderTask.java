package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zgw
 * @date 2022/10/10 14:14
 * @mood
 */
@Component @JobHandler(value = "batteryMemberCardExpireReminderTask") @Slf4j
public class BatteryMemberCardExpireReminderTask extends IJobHandler {

    @Autowired ElectricityMemberCardOrderService electricityMemberCardOrderService;

    //电池套餐快过期提醒
    @Override public ReturnT<String> execute(String param) throws Exception {
        try {
            electricityMemberCardOrderService.batteryMemberCardExpireReminder();
        } catch (Exception e) {
            log.error("xxl-job电池月卡即将过期提醒处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
