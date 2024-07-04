package com.xiliulou.electricity.vo.enterprise;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/7/3 18:28
 * @desc
 */
@Data
public class CloudBeanUseRecordExcelVO {
    @ExcelProperty("企业名称")
    private String enterpriseName;
    
    @ExcelProperty("支出/收入")
    private String incomeAndExpend;
    @ExcelProperty("消费类型")
    private String consumerType;
    @ExcelProperty("云豆数")
    private BigDecimal beanAmount;
    @ExcelProperty("消费时间")
    private String createTime;
}
