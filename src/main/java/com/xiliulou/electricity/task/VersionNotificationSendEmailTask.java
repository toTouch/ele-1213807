package com.xiliulou.electricity.task;

import com.xiliulou.electricity.entity.EmailRecipient;
import com.xiliulou.electricity.entity.MQMailMessageNotify;
import com.xiliulou.electricity.entity.TenantNotifyMail;
import com.xiliulou.electricity.entity.VersionNotification;
import com.xiliulou.electricity.service.MailService;
import com.xiliulou.electricity.service.TenantNotifyMailService;
import com.xiliulou.electricity.service.VersionNotificationService;
import com.xiliulou.electricity.vo.TenantNotifyMailVO;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private static final Integer limit = 5;

    @Autowired
    private VersionNotificationService versionNotificationService;
    @Autowired
    private TenantNotifyMailService tenantNotifyMailService;
    @Autowired
    private MailService mailService;

//    @Value("${spring.mail.username}")
//    private String from;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //1.获取最新未发送邮件通知的版本升级记录
        VersionNotification versionNotification = versionNotificationService.selectNotSendMailOne();
        if (Objects.isNull(versionNotification)) {
            log.warn("ELE ERROR!versionNotification is null");
            return IJobHandler.FAIL;
        }


        //2.每次获取limit个邮箱 发送通知
        int i = 0;
        while (true) {
            List<TenantNotifyMailVO> tenantNotifyMailList = tenantNotifyMailService.selectByPage(i, i += limit);
            if (CollectionUtils.isEmpty(tenantNotifyMailList)) {
                break;
            }

            List<EmailRecipient> mailList = tenantNotifyMailList.stream().map(item -> {
                EmailRecipient emailRecipient = new EmailRecipient();
                emailRecipient.setEmail(item.getMail());
                emailRecipient.setName(item.getTenantName());
                return emailRecipient;
            }).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(mailList)) {
                log.error("ELE ERROR!mailList is empty");
                return IJobHandler.FAIL;
            }

            MQMailMessageNotify mailMessageNotify = MQMailMessageNotify.builder()
                    .to(mailList)
                    .subject(versionNotification.getVersion())
                    .text(versionNotification.getContent()).build();

            mailService.sendVersionNotificationEmailToMQ(mailMessageNotify);
        }


        //3.发送完毕 更新邮件发送状态
        VersionNotification updateVersionNotification = new VersionNotification();
        updateVersionNotification.setId(versionNotification.getId());
        updateVersionNotification.setSendMailStatus(VersionNotification.STATUS_SEND_MAIL_YES);
        updateVersionNotification.setUpdateTime(System.currentTimeMillis());
        versionNotificationService.update(updateVersionNotification);

        return IJobHandler.SUCCESS;
    }
}
