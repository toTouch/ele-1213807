package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/22 09:49
 */
@Data
public class ElePowerMonthRecordExcelVo {
    @ExcelProperty("柜机名称")
    private String eName;

    @ExcelProperty("门店名称")
    private String storeName;

    @ExcelProperty("加盟商名称")
    private String franchiseeName;
    /**
     * 月初耗电量
     */
    @ExcelProperty("月初度数")
    private Double monthStartPower;
    /**
     * 月末耗电量
     */
    @ExcelProperty("月末度数")
    private Double monthEndPower;
    /**
     * 本月耗电量
     */
    @ExcelProperty("本月用电量")
    private Double monthSumPower;
    /**
     * 本月电费
     */
    @ExcelProperty("本月电费")
    private Double monthSumCharge;
    /**
     * 类别明细
     */
    @ExcelProperty("类别明细")
    private String typeDetail;
    /**
     * 日期
     */
    @ExcelProperty("出账年月")
    private String date;

}
