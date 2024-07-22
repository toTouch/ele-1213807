package com.xiliulou.electricity.vo.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/1/2 9:47
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleHardwareFailureWarnMsgPageVo {
    /**
     * 设备sn
     */
    private String sn;
    
    /**
     * 设备分类：1-电池  2-换电柜
     */
    private Integer deviceType;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 故障名称
     */
    private String failureAlarmName;
    
    /**
     * 格挡号
     */
    private Integer cellNo;
    
    /**
     * 等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    private Integer grade;
    
    /**
     * 告警标识: 1：告警中，0：已恢复
     */
    private Integer alarmFlag;
    
    /**
     * 恢复时间
     */
    private Long recoverTime;
    
    
    /**
     * 告警时间
     */
    private Long alarmTime;
    
    private Long id;
    /**
     * 换电柜Id
     */
    private Integer cabinetId;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 报文类型：410
     */
    private Integer msgType;
    
    /**
     * 流水号
     */
    private String txnNo;
    
    /**
     * 信号量
     */
    private String signalId;
    
    /**
     * 告警事件描述
     */
    private String alarmDesc;
    
    /**
     * 告警消息Id
     */
    private String alarmId;
    
    /**
     * 类型：0-告警，1-故障
     */
    private Integer type;
    
    /**
     * 故障发生次数
     */
    private Integer occurNum;
    
    /**
     * 设备Id
     */
    private String devId;
    
    /**
     * 上报时间
     */
    private Long reportTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    
    private Long updateTime;
    
    /**
     * 换电柜地址
     */
    private String address;
    
    /**
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 柜机版本
     */
    private String cabinetVersion;
    
    /**
     * 电池sn
     */
    private String batterySn;
    
    /**
     * 柜机编号
     */
    private String cabinetSn;
}
