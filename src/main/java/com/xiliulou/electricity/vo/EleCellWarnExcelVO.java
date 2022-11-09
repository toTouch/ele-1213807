package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EleCellWarnExcelVO {
    
    @ExcelProperty("序号")
    private Integer id;
    
    @ExcelProperty("柜机")
    private String cabinetName;
    
    @ExcelProperty("格挡")
    private Integer cellNo;
    
    @ExcelProperty("描述")
    private String errorMsg;
    
    @ExcelProperty("告警时间")
    private String reportTime;
    
    @ExcelProperty("错误类型")
    private String errorType;
    
    @ExcelProperty("操作类型")
    private String operateType;
    
}
