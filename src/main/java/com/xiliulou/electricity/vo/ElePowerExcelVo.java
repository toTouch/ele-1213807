package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/22 09:27
 */
@Data
public class ElePowerExcelVo {
    @ExcelProperty("电柜名称")
    private String eName;

    @ExcelProperty("上报时间")
    private String reportTime;

    @ExcelProperty("总耗电量")
    private Double sumPower;

    @ExcelProperty("每小时耗电量")
    private Double hourPower;
    /**
     * 每小时电费
     */
    @ExcelProperty("每小时电费")
    private Double electricCharge;
}
