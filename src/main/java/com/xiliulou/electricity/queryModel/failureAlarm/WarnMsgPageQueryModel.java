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
public class WarnMsgPageQueryModel {
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
     * alarmId
     */
    private String alarmId;
    
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
    
    /**
     * 批次号
     */
    private String batchNo;
    
    /**
     * 消息可见(0-不可见， 1-可见)
     */
    private Integer msgVisible;
    
    /**
     * 运作状态(0-启用， 1-禁用)
     */
    private Integer enableStatus;
    
    /**
     * 处理方式：0--待处理 1--处理中 2--已处理  3--忽略 4--转工单 5--自动恢复
     */
    private Integer handleStatus;
}
