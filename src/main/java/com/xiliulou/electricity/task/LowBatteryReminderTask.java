package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Hardy
 * @date 2021/12/1 18:55
 * @mood
 */
@Component
@JobHandler(value = "lowBatteryReminderTask")
@Slf4j
public class LowBatteryReminderTask extends IJobHandler {

    @Autowired
    ElectricityBatteryService electricityBatteryService;

    //定时任务--电池电量不足提示
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            electricityBatteryService.handlerLowBatteryReminder();
        } catch (Exception e) {
            log.error("处理失败",e);
        }
        return IJobHandler.SUCCESS;
    }
}
