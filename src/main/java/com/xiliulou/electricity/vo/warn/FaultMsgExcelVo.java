package com.xiliulou.electricity.vo.warn;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 故障告警记录导出
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaultMsgExcelVo {
    /**
     * 设备编号
     */
    @ExcelProperty("设备编号")
    private String sn;
    
    /**
     *设备分类：1-电池  2-换电柜
     */
    @ExcelProperty("设备分类")
    private String deviceType;
    
    /**
     * 故障名称
     */
    @ExcelProperty("告警名称")
    private String failureAlarmName;
    
    /**
     * 格口
     */
    @ExcelProperty("格口")
    private Integer cellNo;
    
    /**
     *等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    @ExcelProperty("等级")
    private String grade;
    
    /**
     * 告警标识
     */
    private Integer alarmFlag;
    
    @ExcelProperty("状态")
    private String alarmFlagExport;
    
    /**
     * 告警时间
     */
    @ExcelProperty("告警时间")
    private String alarmTime;
    
    /**
     * 恢复时间
     */
    @ExcelProperty("恢复时间")
    private String recoverTime;
    
    /**
     * 租户名称
     */
    @ExcelProperty("租户名称")
    private String tenantName;
    
    /**
     * 柜机id
     */
    private Integer cabinetId;
    
    /**
     * 信号量
     */
    private String signalId;
    
    private String alarmDesc;
    
    /**
     * 电池sn
     */
    private String batterySn;
    
    /**
     * 电池id
     */
    private Long batteryId;
    
}
