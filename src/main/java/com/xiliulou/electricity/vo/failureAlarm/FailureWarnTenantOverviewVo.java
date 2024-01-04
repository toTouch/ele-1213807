package com.xiliulou.electricity.vo.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/1/3 10:14
 * @desc 故障统计分析：运营商总览
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureWarnTenantOverviewVo {
    /**
     * 租户Id
     */
    private Integer tenantId;
    
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
     * 柜机出货量
     */
    private Integer cabinetShipment;
    
    /**
     * 故障率
     */
    private BigDecimal failureRate;
    
    
    /**
     * 告警频率
     */
    private BigDecimal warnRate;
}
