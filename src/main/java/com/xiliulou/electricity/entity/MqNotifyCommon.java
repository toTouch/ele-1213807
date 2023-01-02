package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2022/4/12 10:53
 */
@Data
public class MqNotifyCommon<T> {
    /**
     * 通知的类型
     */
    private Integer type;
    /**
     * 通知时间
     */
    private Long time;
    /**
     * 通知人的手机号
     */
    private String phone;
    /**
     * 详细通知内容
     */
    private T data;

    /**
     * 硬件消息
     */
    public static final Integer TYPE_HARDWARE_INFO = 0;
    /**
     * 设备消息
     */
    public static final Integer TYPE_DEVICE_LOG = 1;

    public static final Integer TYPE_MALFUNCTION_INFO = 2;

    /**
     * 系统升级邮件通知
     */
    public static final Integer TYPE_UPGRADE_SEND_MAIL = 10000;
    
    /**
     * 故障通知告警
     */
    public static final Integer TYPE_ABNORMAL_ALARM = 6;
    
    /**
     * 实名认证审核通知
     */
    public static final Integer TYPE_AUTHENTICATION_AUDIT=9;

    public static final Integer TYPE_DISABLE_MEMBER_CARD = 10;

    public static final String PROJECT_NAME = "换电柜";
}
