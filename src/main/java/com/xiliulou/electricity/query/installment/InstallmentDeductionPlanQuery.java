package com.xiliulou.electricity.query.installment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/5 23:20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDeductionPlanQuery {
    
    /**
     * 请求签约号，唯一
     */
    private String externalAgreementNo;
    
    /**
     * 扣款订单号，关联对应的最新一条代扣记录
     */
    private String payNo;
    
    /**
     * 分期期次
     */
    private Integer issue;
    
    /**
     * 应还款金额
     */
    private BigDecimal amount;
    
    /**
     * 分期套餐id
     */
    private Long packageId;
    
    /**
     * 分期套餐类型，0-换电，1-租车，2-车电一体
     */
    private Integer packageType;
    
    /**
     * 租期，单位天
     */
    private Integer rentTime;
    
    /**
     * 应还款时间
     */
    private Long deductTime;
    
    /**
     * 应还款时间
     */
    private Long paymentTime;
    
    /**
     * 支付状态
     */
    private Integer status;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long createTime;
    
    private Long updateTime;
    
    private List<Long> franchiseeIds;
    
    private List<Integer> statuses;
    
    private Long endTime;
}
