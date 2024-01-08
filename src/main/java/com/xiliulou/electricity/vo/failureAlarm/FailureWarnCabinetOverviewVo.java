package com.xiliulou.electricity.vo.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/1/3 10:14
 * @desc 故障统计分析：设备总览
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureWarnCabinetOverviewVo {
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 租户Id
     */
    private Integer cabinetId;
    
    /**
     * 设备编号
     */
    private String sn;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 故障次数
     */
    private Integer failureCount;
    
    /**
     * 告警次数
     */
    private Integer warnCount;
    
    /**
     * 使用天数
     */
    private Integer useDays;
    
    /**
     * 失败率
     */
    private BigDecimal failureRate;
}
