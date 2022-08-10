package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.EmailRecipient;
import com.xiliulou.electricity.entity.MQMailMessageNotify;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.MailService;
import com.xiliulou.electricity.service.TenantNotifyMailService;
import com.xiliulou.electricity.vo.TenantNotifyMailVO;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-09-15:52
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_EMAIL_WARN_MSG_HANDLER)
@Slf4j
public class NormalEleEmailWarnMsgHandler extends AbstractElectricityIotHandler {

    private static final String SUBJECT_PREFIX = "柜机告警通知：";

    @Autowired
    private TenantNotifyMailService tenantNotifyMailService;

    @Autowired
    private MailService mailService;


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        EmailWarnMsgVO emailWarnMsgVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EmailWarnMsgVO.class);
        if (Objects.isNull(emailWarnMsgVO)) {
            log.error("ELE ERROR! emailWarnMsgVO is null,sessionId={}", receiverMessage.getSessionId());
            return;
        }

        //1.获取租户绑定的邮箱
        List<TenantNotifyMailVO> tenantNotifyMails = tenantNotifyMailService.selectByTenantId(electricityCabinet.getTenantId().longValue());
        if (CollectionUtils.isEmpty(tenantNotifyMails)) {
            log.warn("ELE ERROR!,tenantNotifyMails is empty,tenantid={},sessionId={}", electricityCabinet.getTenantId(), receiverMessage.getSessionId());
            return;
        }

        List<EmailRecipient> mailList = tenantNotifyMails.stream().map(item -> {
            EmailRecipient emailRecipient = new EmailRecipient();
            emailRecipient.setEmail(item.getMail());
            emailRecipient.setName(item.getTenantName());
            return emailRecipient;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(mailList)) {
            log.error("ELE ERROR!mailList is empty");
            return;
        }

        //2.发送邮件消息
        MQMailMessageNotify mailMessageNotify = MQMailMessageNotify.builder()
                .to(mailList)
                .subject(SUBJECT_PREFIX + electricityCabinet.getName())
                .text(emailWarnMsgVO.warnMsg).build();

        mailService.sendVersionNotificationEmailToMQ(mailMessageNotify);

    }


    @Data
    class EmailWarnMsgVO {
        private String warnMsg;

        private Long createTime;
    }
}
