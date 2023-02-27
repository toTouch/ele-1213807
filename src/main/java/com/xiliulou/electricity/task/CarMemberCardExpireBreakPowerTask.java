package com.xiliulou.electricity.task;

import com.xiliulou.electricity.entity.UserCarMemberCard;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zgw
 * @date 2023/2/15 11:01
 * @mood
 */
@Component
@JobHandler(value = "carMemberCardExpireBreakPowerTask")
@Slf4j
public class CarMemberCardExpireBreakPowerTask extends IJobHandler {
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    //车辆套餐过期断电
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            userCarMemberCardService.expireBreakPowerHandel();
        } catch (Exception e) {
            log.error("xxl-job车辆套餐过期断电处理失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
