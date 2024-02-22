package com.xiliulou.electricity.vo.failureAlarm;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/1/24 16:55
 * @desc 故障告警占比导出Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureWarnProportionExportVo {
    @ExcelProperty("序号")
    private Integer serialNumber;
    
    @ExcelProperty("等级")
    private String grade;
    
    @ExcelProperty("设备分类")
    private String deviceType;
    
    @ExcelProperty("信号量ID")
    private String signalId;
    
    @ExcelProperty("信号标准名")
    private String signalName;
    
    @ExcelProperty("次数")
    private Integer num;
}
