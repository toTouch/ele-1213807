package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 保险过期更新保险订单状态
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-08-08-15:25
 */
@Component
@Slf4j
@JobHandler(value = "insuranceExpireUpdateStatusTask")
public class InsuranceExpireUpdateStatusTask extends IJobHandler {

    @Autowired
    private InsuranceUserInfoService insuranceUserInfoService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {

        try {
            insuranceUserInfoService.updateUserInsuranceOrderStatusTask();
        } catch (Exception e) {
            log.error("xxl-job保险过期更新保险订单状态失败", e);
        }
        return IJobHandler.SUCCESS;

    }
}
