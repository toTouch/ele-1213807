package com.xiliulou.electricity.request.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 故障预警设置 查询请求
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureAlarmPageRequest {
    
    /**
     * 名称
     */
    private String signalName;
    /**
     * 信号量ID
     */
    private String signalId;
    /**
     * 分类(1-故障， 2-告警)
     */
    private Integer type;
    /**
     *等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    private Integer grade;
    
    /**
     * 保护措施
     */
    private List<Long> protectMeasureList;
    
    /**
     * 运作状态(0-启用， 1-禁用)
     */
    private Integer status;
    
    /**
     *设备分类：1-电池  2-换电柜
     */
    private Integer deviceType;
    
    /**
     * 运营商可见(0-不可见， 1-可见)
     */
    private Integer tenantVisible;
    
    private Long size;
    
    private Long offset;

}
