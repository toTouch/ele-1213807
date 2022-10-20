package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.MQMailMessageNotify;

public interface MailService {

    void sendVersionNotificationEmailToMQ(MQMailMessageNotify mailMessage);
}
