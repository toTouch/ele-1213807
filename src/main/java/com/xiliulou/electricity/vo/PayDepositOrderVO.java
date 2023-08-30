package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Hardy
 * @date 2021/12/3 17:37
 * @mood
 */
@Data
public class PayDepositOrderVO {
    private Long id;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 缴纳时间
     */
    private Long payTime;

    /**
     * 退款时间
     */
    private Long refundTime;

    /**
     * 退款状态
     */
    private Integer refundStatus;

    /**
     * 缴纳押金订单号
     */
    private String orderId;
    
    /**
     * 电池型号
     */
    private Integer model;


    private Integer payType;

    /**
     * 电池型号
     */
    private String batteryType;
}
