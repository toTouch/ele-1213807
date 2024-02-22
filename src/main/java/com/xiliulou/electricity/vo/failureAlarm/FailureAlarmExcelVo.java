package com.xiliulou.electricity.vo.failureAlarm;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 故障预警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureAlarmExcelVo {
    /**
     * 错误码
     */
    @ExcelProperty("序号")
    private Integer serialNumber;
    
    /**
     * 错误码
     */
    @ExcelProperty("信号量ID")
    private String signalId;
    
    /**
     * 名称
     */
    @ExcelProperty("信号量标准名")
    private String signalName;
    
    /**
     * 分类(1-故障， 2-告警)
     */
    @ExcelProperty("类型")
    private String type;
    
    /**
     *设备分类：1-电池  2-换电柜
     */
    @ExcelProperty("设备分类")
    private String deviceType;

    /**
     * 信号说明
     */
    @ExcelProperty("信号说明")
    private String signalDesc;
    
    /**
     * 事件描述
     */
    @ExcelProperty("事件描述")
    private String eventDesc;
    
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
    @ExcelProperty("运作状态")
    private String status;
    
    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    private String createTime;
}
