/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.enums.notify;

import lombok.Getter;

/**
 * description: 消息发送类型
 *
 * @author caobotao.cbt
 * @date 2024/6/26 18:10
 */
@Getter
public enum SendMessageTypeEnum {
    DEVICE_LOGIN_NOTIFY(1, "设备上下线"),
    //    REFUND_NOTIFY(2,"退款通知"),todo: 程序未找到此通知
    ABNORMAL_ALARM_NOTIFY(6, "故障上报通知"),
    AUTHENTICATION_AUDIT_NOTIFY(9, "实名认证审核通知"),
    RENTAL_PACKAGE_FREEZE_AUDIT_NOTIFY(10, "套餐冻结审核通知"),
    HIGH_WARNING_NOTIFY(12, "高温预警"),
    REFUND_RENT_AUDIT_NOTIFY(13, "退租审核通知"),
    //    UPGRADE_SEND_MAIL_NOTIFY(10000,"系统升级邮件通知"),
    ;
    
    
    private Integer type;
    
    private String desc;
    
    
    SendMessageTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
