package com.xiliulou.electricity.entity.warn;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author maxiaodong
 * @description 告警信息
 * @date 2024/05/23 11:55:39
 */
@TableName("t_ele_hardware_fault_msg")
@Data
public class EleHardwareFaultMsg {
    /**
     * 设备sn
     */
    private String sn;
    
    /**
     * 告警消息Id
     */
    private String alarmId;
    
    /**
     * 格挡号
     */
    private Integer cellNo;
    
    /**
     * 设备分类：1-电池  2-换电柜
     */
    private Integer deviceType;
    
    /**
     * 信号量
     */
    private String signalId;
    
    /**
     * 故障时间
     */
    private String alarmTime;
    
    /**
     * 换电柜Id
     */
    private Integer cabinetId;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    private Integer grade;
    
    /**
     * 设备Id
     */
    private String devId;
    
    /**
     * 告警事件描述
     */
    private String alarmDesc;
    
    /**
     * 上报时间
     */
    private String reportTime;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 柜机编号
     */
    private String cabinetSn;

  
    
    /**
     * 换电柜类型
     */
    public static final Integer DEVICE_TYPE_BATTERY = 1;
    
    public static final Integer DEVICE_TYPE_CABINET = 2;
    
    /**
     * 等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    public static final Integer FIRST_LEVEL = 1;
    public static final Integer SECOND_LEVEL = 2;
    public static final Integer THIRD_LEVEL = 3;
    public static final Integer FOURTH_LEVEL = 4;
    
}
