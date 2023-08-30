package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 换电套餐过期  更新换电套餐订单状态
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-08-09-11:29
 */
@Component
@Slf4j
@JobHandler(value = "batteryMembercardExpireUpdateStatusTask")
public class BatteryMembercardExpireUpdateStatusTask extends IJobHandler {

    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            userBatteryMemberCardService.batteryMembercardExpireUpdateStatusTask();
        } catch (Exception e) {
            log.error("xxl-job换电套餐过期更新套餐订单状态失败", e);
        }
        return IJobHandler.SUCCESS;
    }
}
