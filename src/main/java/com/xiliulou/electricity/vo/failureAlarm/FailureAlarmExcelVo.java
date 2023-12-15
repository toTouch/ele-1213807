package com.xiliulou.electricity.vo.failureAlarm;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 故障预警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */

@Data
public class FailureAlarmExcelVo {
    
    /**
     * 错误码
     */
    @ExcelProperty("错误码")
    private Integer errorCode;
    
    /**
     * 名称
     */
    @ExcelProperty("名称")
    private String name;
    
    /**
     * 分类(1-故障， 2-告警)
     */
    @ExcelProperty("分类")
    private String type;
    
    /**
     *模块(1- 主板， 2- 子板，3- 电池，4 -电池异常消失，5 -车辆，6-充电器，7-BMS)
     */
    @ExcelProperty("模块")
    private String model;
    
    /**
     * 触发规则
     */
    @ExcelProperty("触发规则")
    private String triggerRules;
    
    /**
     * 保护措施
     */
    @ExcelProperty("保护措施")
    private String protectMeasure;
    
    /**
     *等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    @ExcelProperty("等级")
    private String grade;
    
    /**
     * 运营商可见(0-不可见， 1-可见)
     */
    @ExcelProperty("运营商可见")
    private String tenantVisible;
    
    /**
     * 运作状态(0-启用， 1-禁用)
     */
    @ExcelProperty("运营商可见")
    private String status;
    
    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    private String createTime;
}
