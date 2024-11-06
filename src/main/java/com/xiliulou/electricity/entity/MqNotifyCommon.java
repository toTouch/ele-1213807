package com.xiliulou.electricity.entity;

import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import lombok.Data;

/**
 * @author : eclair
 * @date : 2022/4/12 10:53
 */
@Data
public class MqNotifyCommon<T> {
    
    /**
     * 通知的类型
     * @see SendMessageTypeEnum
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
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 详细通知内容
     */
    private T data;
    
    /**
     * 链路id
     */
    private String traceId;
    
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
     * 高温告警
     */
    public static final Integer TYPE_HIGH_TEMPERATURE_ALARM = 12;
    
    /**
     * 退租审核
     */
    public static final Integer TYPE_RENT_REFUND_AUDIT = 13;
    
    /**
     * 实名认证审核通知
     */
    public static final Integer TYPE_AUTHENTICATION_AUDIT = 9;
    
    public static final Integer TYPE_DISABLE_MEMBER_CARD = 10;
    
    public static final String PROJECT_NAME = "换电柜";
}
