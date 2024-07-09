package com.xiliulou.electricity.task.warn;

import com.xiliulou.electricity.service.warn.EleHardwareFaultCabinetMsgService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author maxiaodong
 * @date 2023/12/28 18:55
 * @mood 故障告警柜机日结统计
 */
@Component
@JobHandler(value = "hardwareFaultCabinetMsgTask")
@Slf4j
public class HardwareFaultCabinetMsgTask extends IJobHandler {
    
    @Autowired
    private EleHardwareFaultCabinetMsgService faultCabinetMsgService;
    
    //定时任务--统计每日换电柜上的故障和告警数量
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        log.info("hardware fault Cabinet Msg  start");
        
        try {
            faultCabinetMsgService.createFaultWarnData();
        } catch (Exception e) {
            log.error("hardware fault Cabinet Msg error",e);
        }
        
        log.info("hardware Failure Cabinet Msg  end");
        
        return IJobHandler.SUCCESS;
    }
}
