package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName: MutualElectricityBatteryExcelVO
 * @description: 互通电池导出
 * @author: renhang
 * @create: 2024-11-29 09:19
 */
@Data
public class MutualElectricityBatteryExcelVO {
    
    
    @ExcelProperty("电池编号")
    private String sn;
    
    @ExcelProperty("所属加盟商")
    private String franchiseeName;
    
    @ExcelProperty("是否在仓")
    private String physicsStatus;
    
    @ExcelProperty("所在电柜")
    private String electricityCabinetName;
    
    @ExcelProperty("所在仓门")
    private Integer cellNo;
    
    @ExcelProperty("当前用户")
    private String userName;
    
    @ExcelProperty("手机号")
    private String phone;
    
    @ExcelProperty("互通的加盟商")
    private String mutualFranchiseeName;
    
}
