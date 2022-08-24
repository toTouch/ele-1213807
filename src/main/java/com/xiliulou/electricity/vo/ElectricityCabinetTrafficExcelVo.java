package com.xiliulou.electricity.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import java.math.BigDecimal;
import lombok.Data;

/**
 * @author hrp
 * @since 2022-08-24 11:13:45
 */
@Data
public class ElectricityCabinetTrafficExcelVo {

    @ExcelProperty("序号")
    private Integer id;

    @ExcelProperty("电柜名称")
    private String electricityCabinetName;

    @ExcelProperty("当天流量")
    private BigDecimal sameDayTraffic;

    @ExcelProperty("总流量")
    private BigDecimal sumTraffic;

    @ExcelProperty("日期")
    private String date;

    @ExcelProperty("创建时间")
    private String createTime;

    @ExcelProperty("更新时间")
    private String updateTime;

}
