package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@JobHandler(value = "electricityCabinetStatisticTask")
public class ElectricityCabinetStatisticTask extends IJobHandler {
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            electricityCabinetService.handleElectricityCabinetStatistic(s);
        } catch (Exception e) {
            log.error("xxl-job electricityCabinet statistic error", e);
        }
        return IJobHandler.SUCCESS;
    }
}
