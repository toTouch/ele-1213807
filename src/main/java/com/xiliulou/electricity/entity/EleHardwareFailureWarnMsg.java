package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author maxiaodong
 * @description 新故障告警信息
 * @date 2023/12/26 11:55:39
 */
@TableName("t_ele_hardware_failure_warn_msg")
@Data
public class EleHardwareFailureWarnMsg {
    @TableId(value = "id", type = IdType.AUTO)
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
     * 告警标识
     */
    private Integer alarmFlag;
    
    /**
     * 告警消息Id
     */
    private String alarmId;
    
    /**
     * 格挡号
     */
    private Integer cellNo;
    
    /**
     * 设备sn
     */
    private String sn;
    
    /**
     * 类型：0-告警，1-故障
     */
    private Integer type;
    
    /**
     * 故障发生次数
     */
    private Integer occurNum;
    
    /**
     * 等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    private Integer grade;
    
    /**
     * 设备分类：1-电池  2-换电柜
     */
    private Integer deviceType;
    
    /**
     * 故障名称
     */
    private String failureAlarmName;
    
    /**
     * 设备Id
     */
    private String devId;
    
    /**
     * 恢复时间
     */
    private Long recoverTime;
    
    /**
     * 上报时间
     */
    private Long reportTime;
    
    /**
     * 告警时间
     */
    private Long alarmTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    
    private Long updateTime;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 换电柜地址
     */
    @TableField(exist = false)
    private String address;
    
    /**
     * 柜机名称
     */
    @TableField(exist = false)
    private String cabinetName;
    
    /**
     * 类型
     */
    public static final Integer FAILURE = 1;
    public static final Integer WARN = 0;
    
    /**
     * 告警标识
     */
    public static final Integer WARN_START = 1;
    
    public static final Integer WARN_END = 0;
    
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
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof EleHardwareFailureWarnMsg) {
            EleHardwareFailureWarnMsg failureStatistics = (EleHardwareFailureWarnMsg) o;
            return this.alarmId.equals(failureStatistics.alarmId);
        }
        return super.equals(o);
    }
    
}
