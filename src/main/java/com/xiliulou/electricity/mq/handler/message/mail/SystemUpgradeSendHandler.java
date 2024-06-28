/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/28
 */

package com.xiliulou.electricity.mq.handler.message.mail;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import org.springframework.stereotype.Component;

/**
 * description: 系统升级邮件发送
 *
 * @author caobotao.cbt
 * @date 2024/6/28 15:35
 */
@Component
public class SystemUpgradeSendHandler extends AbstractMallSendHandler {
    
    @Override
    public Integer getType() {
        return SendMessageTypeEnum.UPGRADE_SEND_MAIL_NOTIFY.getType();
    }
}
