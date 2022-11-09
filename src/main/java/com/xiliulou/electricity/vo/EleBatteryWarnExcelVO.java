package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EleBatteryWarnExcelVO {

    @ExcelProperty("序号")
    private Integer id;
    @ExcelProperty("柜机")
    private String cabinetName;
    @ExcelProperty("电池编号")
    private String batteryName;
    @ExcelProperty("描述")
    private String errorMsg;
    @ExcelProperty("告警时间")
    private String reportTime;





}
