package com.xiliulou.electricity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/3 20:57
 */
@Data
public class InstallmentMqCommonDTO {
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 请求签约号
     */
    private String externalAgreementNo;
    
    /**
     * 代扣的期次
     */
    private Integer issue;
    
    /**
     * 每一个代扣计划代扣的金额
     */
    private BigDecimal amount;
    
    /**
     * 代扣计划id
     */
    private Long deductionPlanId;
    
    /**
     * 代扣记录id
     */
    private Long deductionRecordId;
    
    /**
     * 签约操作的traceId，用于追踪异步代扣
     */
    private String traceId;
}
