package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 退押
 * @date 2024/12/30 16:41:26
 */
@Data
public class EleDepositRefundVO {
    
    private Integer status;
    
    private Integer orderType;
    
    private Boolean refundFlag;
    
    private String orderId;
    
    private Integer payType;
    
    /**
     * 可退款金额
     */
    private Double payTransAmt;
    
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
}
