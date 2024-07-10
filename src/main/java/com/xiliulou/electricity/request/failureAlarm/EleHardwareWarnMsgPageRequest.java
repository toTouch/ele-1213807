package com.xiliulou.electricity.request.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/1/2 9:23
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EleHardwareWarnMsgPageRequest {
    /**
     * 类型：0-告警，1-故障
     */
    private Integer type;
    
    /**
     * 设备sn
     */
    private String sn;
    
    /**
     * 运营商
     */
    private Integer tenantId;
    
    /**
     * 设备分类：1-电池  2-换电柜
     */
    private Integer deviceType;
    
    /**
     * 等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    private Integer grade;
    
    /**
     * 故障名称
     */
    private String signalId;
    
    /**
     * 告警/告警开始时间
     */
    private Long alarmStartTime;
    
    /**
     * 告警/告警结束时间
     */
    private Long alarmEndTime;
    
    /**
     * 状态：1：告警中，0：已恢复
     */
    private Integer alarmFlag;
    
    /**
     * 运营商可见(0-不可见， 1-可见)
     */
    private Integer tenantVisible;
    
    /**
     * 运作状态(0-启用， 1-禁用)
     */
    private Integer status;
    
    private Long size;
    
    private Long offset;
    
    /**
     * 导出查询添加限制
     */
    private Integer days;
    
    /**
     * 不限制故障告警设置
     */
    private Integer noLimitSignalId;
    
    /**
     * 柜机Id
     */
    private Integer cabinetId;
    
    private String alarmId;
    
    private List<String> signalIdList;
}
