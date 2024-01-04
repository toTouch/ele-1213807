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
public class TenantOverviewWarnExportVo {
    /**
     * 租户名称
     */
    @ExcelProperty("租户名称")
    private String tenantName;
    
    /**
     * 告警次数
     */
    @ExcelProperty("告警次数")
    private Integer warnCount;
    
    /**
     * 柜机出货量
     */
    @ExcelProperty("柜机出货量")
    private Integer cabinetShipment;
    
    /**
     * 告警频率
     */
    @ExcelProperty("告警率")
    private BigDecimal warnRate;
}
