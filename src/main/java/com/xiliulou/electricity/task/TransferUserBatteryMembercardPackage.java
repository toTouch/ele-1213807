package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-13-17:47
 */
@Component
@Slf4j
@JobHandler(value = "transferPayQuery")
public class TransferUserBatteryMembercardPackage extends IJobHandler {

    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            userBatteryMemberCardPackageService.handlerTransferBatteryMemberCardPackage();
        } catch (Exception e) {
            log.error("用户套餐资源包处理失败",e);
        }
        return IJobHandler.SUCCESS;
    }
}
