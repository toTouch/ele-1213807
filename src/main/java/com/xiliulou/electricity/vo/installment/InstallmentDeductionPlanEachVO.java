package com.xiliulou.electricity.vo.installment;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/5 10:32
 */
@Data
public class InstallmentDeductionPlanEachVO {
    private Long id;
    
    /**
     * 分期期次
     */
    private Integer issue;
    
    /**
     * 扣款订单号，关联对应的最新一条代扣记录
     */
    private String payNo;
    
    /**
     * 扣款交易单号
     */
    private String tradeNo;
    
    /**
     * 应还款金额
     */
    private BigDecimal amount;
    
    /**
     * 实际还款时间
     */
    private Long paymentTime;
    
    /**
     * 支付状态
     */
    private Integer status;
    
    private Long createTime;
    
    private Long updateTime;
}
