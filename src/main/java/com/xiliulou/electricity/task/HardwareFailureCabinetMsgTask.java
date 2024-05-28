package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.EleHardwareFailureCabinetMsgService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2023/12/28 18:55
 * @mood
 */
@Component
@JobHandler(value = "hardwareFailureCabinetMsgTask")
@Slf4j
public class HardwareFailureCabinetMsgTask extends IJobHandler {

    @Autowired
    private EleHardwareFailureCabinetMsgService failureCabinetMsgService;

    //定时任务--统计每日换电柜上的故障和告警数量
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        log.info("hardware Failure Cabinet Msg start");
        
        try {
            // 旧版本故障告警统计
            failureCabinetMsgService.createFailureWarnData();
            
            // 新版本柜机故障告警数量统计
            failureCabinetMsgService.createFailureWarnDataV2();
        } catch (Exception e) {
            log.error("hardware Failure Cabinet Msg error",e);
        }
        
        log.info("hardware Failure Cabinet Msg end");
        
        return IJobHandler.SUCCESS;
    }
}
