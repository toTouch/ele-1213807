package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hrp
 * @date 2022/11/20 11:07
 * @mood
 */
@Component
@JobHandler(value = "systemEnableMemberCardTask")
@Slf4j
public class SystemEnableMemberCardTask extends IJobHandler {
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    //系统启用停卡中套餐  每小时一次
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            electricityMemberCardOrderService.systemEnableMemberCardTask();
        } catch (Exception e) {
            log.error("xxl-job启用停卡中套餐处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
