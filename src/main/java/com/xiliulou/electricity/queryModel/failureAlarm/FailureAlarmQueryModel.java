package com.xiliulou.electricity.queryModel.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 故障预警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FailureAlarmQueryModel {
    
    /**
     * 名称
     */
    private String name;
    /**
     * 错误码
     */
    private Integer errorCode;
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
     *模块(1- 主板， 2- 子板，3- 电池，4 -电池异常消失，5 -车辆，6-充电器，7-BMS)
     */
    private Integer model;
    
    /**
     * 运营商可见(0-不可见， 1-可见)
     */
    private Integer tenantVisible;
    
    private Long size;
    
    private Long offset;
    
}
