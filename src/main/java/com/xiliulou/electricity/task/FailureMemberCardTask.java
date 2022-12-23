package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.MemberCardFailureRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hrp
 * @date 2022/12/20 11:07
 * @mood
 */
@Component
@JobHandler(value = "failureMemberCardTask")
@Slf4j
public class FailureMemberCardTask extends IJobHandler {
    @Autowired
    MemberCardFailureRecordService memberCardFailureRecordService;

    //系统失效套餐套餐  每小时一次
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            log.info("XXL-JOB---failureMemberCardTask>>>>>开始执行失效套餐查询>>>>>>>");

            memberCardFailureRecordService.failureMemberCardTask();
        } catch (Exception e) {
            log.error("xxl-job启用停卡中套餐处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
