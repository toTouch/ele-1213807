package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EleCabinetWarnExcelVO {
    
    @ExcelProperty("序号")
    private Integer id;
    
    @ExcelProperty("告警时间")
    private String reportTime;
    
    @ExcelProperty("柜机")
    private String cabinetName;
    
    @ExcelProperty("描述")
    private String errorMsg;
    
    
}
