package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.ElectricityCabinetTrafficService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@JobHandler(value = "electricityCabinetTrafficExpiredTask")
@Slf4j
public class ElectricityCabinetTrafficExpiredTask extends IJobHandler {

    @Autowired
    ElectricityCabinetTrafficService electricityCabinetTrafficService;

    //定时任务--清除一年前的电柜流量数据 5分一次
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            electricityCabinetTrafficService.expiredDel();
        } catch (Exception e) {
            log.error("处理失败",e);
        }
        return IJobHandler.SUCCESS;
    }
}
