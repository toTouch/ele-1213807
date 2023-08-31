package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.DivisionAccountRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Kenneth
 * @Date: 2023/7/26 19:41
 * @Description:
 */
@Component
@JobHandler(value = "divisionAccountRecordUpdateStatusTask")
@Slf4j
public class DivisionAccountRecordUpdateStatusTask extends IJobHandler  {

    @Autowired
    private DivisionAccountRecordService divisionAccountRecordService;

    /**
     * 更新分账记录表中超过七天没有退租且分账状态为冻结状态的记录，每天凌晨两点执行一次。0 0 2 * * ?
     *
     * @param s
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        log.info("division account record update status task begin.");
        try {
            divisionAccountRecordService.updateDivisionAccountStatusForFreezeOrder();
        } catch (Exception e) {
            log.error("division account record update status fail", e);
        }
        return IJobHandler.SUCCESS;
    }
}
