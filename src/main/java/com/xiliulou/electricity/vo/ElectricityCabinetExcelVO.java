package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-13-11:21
 */
@Data
public class ElectricityCabinetExcelVO {

    @ExcelProperty("序号")
    private Integer id;

    @ExcelProperty("电柜编码")
    private String sn;

    @ExcelProperty("电柜名称")
    private String name;

    @ExcelProperty("电柜地址")
    private String address;

    @ExcelProperty("运行状态")
    private String usableStatus;

    @ExcelProperty("电柜型号")
    private String modelName;

    @ExcelProperty("电柜类型")
    private String exchangeType;

    @ExcelProperty("版本号")
    private String version;

    @ExcelProperty("加盟商")
    private String franchiseeName;

    @ExcelProperty("服务结束时间")
    private String serverEndTime;

    @ExcelProperty("创建时间")
    private String createTime;

}
