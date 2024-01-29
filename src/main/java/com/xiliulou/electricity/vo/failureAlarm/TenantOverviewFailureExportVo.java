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
public class TenantOverviewFailureExportVo {
    /**
     * 运营商
     */
    @ExcelProperty("运营商")
    private String tenantName;
    
    /**
     * 故障次数
     */
    @ExcelProperty("故障次数")
    private Integer failureCount;
    
    /**
     * 柜机出货量
     */
    @ExcelProperty("柜机出货量")
    private Integer cabinetShipment;
    
    /**
     * 故障率
     */
    @ExcelProperty("故障率")
    private String failureRate;
}
