package com.xiliulou.electricity.vo.failureAlarm;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/1/3 10:14
 * @desc 故障统计分析：运营商总览 故障导出
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CabinetOverviewFailureExportVo {
    /**
     * 柜机编号
     */
    @ExcelProperty("柜机编号")
    private String sn;
    
    /**
     * 故障次数
     */
    @ExcelProperty("故障次数")
    private Integer failureCount;
    
    /**
     * 累计使用天数
     */
    @ExcelProperty("累计使用天数")
    private Integer useDays;
    
    /**
     * 失败率
     */
    @ExcelProperty("失败率")
    private BigDecimal failureRate;
    
    
    /**
     * 所属运营商
     */
    @ExcelProperty("所属运营商")
    private String tenantName;
    

   
   
    
   
}
