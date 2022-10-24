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
 * @date 2022/8/9 9:16
 * @mood
 */
@Component
@JobHandler(value = "memberCardExpireReminderTask")
@Slf4j
public class MemberCardExpireReminderTask extends IJobHandler {

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try{
            electricityMemberCardOrderService.expireReminderHandler();
        } catch (Exception e) {
            log.error("处理失败！", e);
        }
        return IJobHandler.SUCCESS;
    }
}
