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
 * @date 2021/11/25 11:34
 * @mood
 */
@Component
@JobHandler(value = "batteryNotInCabinetWarningTask")
@Slf4j
public class BatteryNotInCabinetWarningTask extends IJobHandler {

    @Autowired
    ElectricityBatteryService electricityBatteryService;


    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            electricityBatteryService.handlerBatteryNotInCabinetWarning();
        } catch (Exception e) {
            log.error("处理失败"+e);
        }
        return IJobHandler.SUCCESS;
    }
}
