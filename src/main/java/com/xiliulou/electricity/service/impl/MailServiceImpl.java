package com.xiliulou.electricity.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.MQMailMessageNotify;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.MailService;
import com.xiliulou.mq.service.RocketMqService;

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
    RocketMqService rocketMqService;

    @Override
    public void sendVersionNotificationEmailToMQ(MQMailMessageNotify mailMessage) {

        MqNotifyCommon<MQMailMessageNotify> query = new MqNotifyCommon<>();
//        query.setPhone(p);
        query.setTime(System.currentTimeMillis());
        query.setType(MqNotifyCommon.TYPE_UPGRADE_SEND_MAIL);
        query.setData(mailMessage);

        Pair<Boolean, String> result = rocketMqService.sendSyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(query), "", "", 0);
        log.info("SEND EMAIL INFO! original msg={}", JsonUtil.toJson(query));
        if (!result.getLeft()) {
            log.error("SEND SIMPLE EMAIL NOTIFY TO MQ ERROR! reason={}", result.getRight());
        }
    }
}
