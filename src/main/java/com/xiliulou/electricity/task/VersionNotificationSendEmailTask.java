package com.xiliulou.electricity.task;

import com.xiliulou.electricity.service.VersionNotificationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 版本升级发送邮件通知
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-09-10:15
 */
@Component
@Slf4j
@JobHandler(value = "versionNotificationSendEmail")
public class VersionNotificationSendEmailTask extends IJobHandler {

    @Autowired
    private VersionNotificationService versionNotificationService;


    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            versionNotificationService.handleVersionNotificationSendEmail();
        } catch (Exception e) {
            log.error("ELE ERROR!version notification send email error!", e);
            return IJobHandler.FAIL;
        }

        return IJobHandler.SUCCESS;
    }
}
