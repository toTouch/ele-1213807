package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.mq.producer.MessageSendProducer;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.MQMailMessageNotify;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.service.MailService;

import lombok.extern.slf4j.Slf4j;

/**
 * 邮件发送
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-08-11:40
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {
    
    @Autowired
    MessageSendProducer messageSendProducer;
    
    @Override
    public void sendVersionNotificationEmailToMQ(MQMailMessageNotify mailMessage) {
        
        MqNotifyCommon<MQMailMessageNotify> query = new MqNotifyCommon<>();
        //        query.setPhone(p);
        query.setTime(System.currentTimeMillis());
        query.setType(SendMessageTypeEnum.UPGRADE_SEND_MAIL_NOTIFY.getType());
        query.setData(mailMessage);

        Pair<Boolean, String> result = messageSendProducer.sendSyncMsg(query, "", "", 0);
        log.info("SEND EMAIL INFO! original msg={}", JsonUtil.toJson(query));
        if (!result.getLeft()) {
            log.error("SEND SIMPLE EMAIL NOTIFY TO MQ ERROR! reason={}", result.getRight());
        }
    }
}
