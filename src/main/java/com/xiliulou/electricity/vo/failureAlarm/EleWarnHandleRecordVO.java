package com.xiliulou.electricity.vo.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/11/7 17:04
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleWarnHandleRecordVO {
    /**
     * 设备sn
     */
    private String sn;
    
    /**
     * 设备分类：1-电池  2-换电柜
     */
    private Integer deviceType;
    
    /**
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 换电柜地址
     */
    private String address;
    
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
     * 告警时间
     */
    private Long alarmTime;
}
