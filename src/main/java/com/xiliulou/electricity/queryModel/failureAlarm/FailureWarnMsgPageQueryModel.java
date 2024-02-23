package com.xiliulou.electricity.queryModel.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/1/2 10:17
 * @desc 故障告警记录分页查询model
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FailureWarnMsgPageQueryModel {
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
    private Integer signalId;
    
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
     * 信号量Id集合
     */
    private List<String> signalIdList;
    
    private Long size;
    
    private Long offset;
    
    /**
     * 柜机Id
     */
    private Integer cabinetId;
}
