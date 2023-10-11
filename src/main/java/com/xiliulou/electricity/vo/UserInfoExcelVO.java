package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @since 2022-09-13 11:13:45
 */
@Data
public class UserInfoExcelVO {
    
    @ExcelProperty("序号")
    private Integer id;
    
    @ExcelProperty("手机号")
    private String phone;
    
    @ExcelProperty("姓名")
    private String name;
    
    @ExcelProperty("当前电池")
    private String nowElectricityBatterySn;
    
    @ExcelProperty("套餐名称")
    private String cardName;
    
    @ExcelProperty("套餐过期时间")
    private String memberCardExpireTime;
    
    @ExcelProperty("押金")
    private BigDecimal batteryDeposit;
    
    @ExcelProperty("邀请人")
    private String inviterUserName;
}
