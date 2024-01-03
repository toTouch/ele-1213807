package com.xiliulou.electricity.vo.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/1/3 10:14
 * @desc 故障统计分析：故障率和设备出货量
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureWarnFrequencyVo {
    /**
     * 设备出货量
     */
    private Integer cabinetShipment;
    
    /**
     * 故障率
     */
    private BigDecimal failureRate;
    
    /**
     * 故障频率环比
     */
    private BigDecimal failureCycleRate;
    
    /**
     * 故障次数
     */
    private Integer failureCount;
    
    /**
     * 告警频率
     */
    private BigDecimal warnRate;
    
    /**
     * 告警频率环比
     */
    private BigDecimal warnCycleRate;
    
    /**
     * 使用天数
     */
    private Integer usageDays;
}
