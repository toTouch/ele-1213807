package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.CarMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zgw
 * @date 2022/10/11 11:07
 * @mood
 */
@Component
@JobHandler(value = "carMemberCardExpireReminderTask")
@Slf4j
public class CarMemberCardExpireReminderTask extends IJobHandler {
    @Autowired
//    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    UserCarMemberCardService userCarMemberCardService;

    //租车套餐快过期提醒  每天凌晨一次
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
//            electricityMemberCardOrderService.carMemberCardExpireReminder();
            userCarMemberCardService.carMemberCardExpireReminder();
        } catch (Exception e) {
            log.error("xxl-job租车月卡即将过期提醒处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
