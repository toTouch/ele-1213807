package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-13-17:09
 */
@Data
public class ElectricityBatteryExcelVO {

    @ExcelProperty("序号")
    private Integer id;

    @ExcelProperty("编码")
    private String sn;

    @ExcelProperty("电池型号")
    private String model;

    @ExcelProperty("所属加盟商")
    private String franchiseeName;

    @ExcelProperty("是否在仓")
    private String physicsStatus;

    @ExcelProperty("业务状态")
    private String businessStatus;

    @ExcelProperty("当前用户")
    private String userName;

    @ExcelProperty("物联网卡号")
    private String iotCardNumber;

    @ExcelProperty("创建时间")
    private String createTime;

}
