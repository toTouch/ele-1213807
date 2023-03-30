package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/13 13:41
 * @mood
 */
@Data
public class JoinShareMoneyActivityHistoryExcelVo {
    
    @ExcelProperty("邀请人用户名")
    private String name;
    
    @ExcelProperty("邀请人手机号")
    private String phone;
    
    @ExcelProperty("参与人用户名")
    private String joinName;
    
    @ExcelProperty("参与人手机号")
    private String joinPhone;
    
    @ExcelProperty("开始时间")
    private String startTime;
    
    @ExcelProperty("过期时间")
    private String expiredTime;
    
    /**
     * 参与状态 1--初始化，2--已参与，3--已过期，4--被替换
     */
    @ExcelProperty("状态")
    private String status;
}
