package com.xiliulou.electricity.vo.enterprise;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-10-09-15:43
 */
@Data
public class CloudBeanOrderExcelVO {
    
    @ExcelProperty("用户名")
    private String username;
    
    @ExcelProperty("手机号")
    private String phone;
    /**
     * 类型 0套餐代付，1套餐回收，2云豆充值，3赠送，4后台充值，5后台扣除
     */
    @ExcelProperty("类型")
    private String type;
    /**
     * 本次使用云豆数量
     */
    @ExcelProperty("云豆数量")
    private BigDecimal beanAmount;
    /**
     * 剩余云豆数量
     */
    @ExcelProperty("变更后云豆数量")
    private BigDecimal remainingBeanAmount;
    
    @ExcelProperty("套餐名称")
    private String packageName;
    
    @ExcelProperty("创建时间")
    private String createTime;
    
    @ExcelProperty("操作人")
    private String operateName;
    
}
