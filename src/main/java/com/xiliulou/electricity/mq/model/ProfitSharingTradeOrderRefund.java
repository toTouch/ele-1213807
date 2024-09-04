package com.xiliulou.electricity.mq.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/8/27 10:36
 * @desc
 */
@Data
public class ProfitSharingTradeOrderRefund {
    /**
     * 业务支付订单号
     */
    private String orderNo;
    
    /**
     * 业务支付订单号
     */
    private String refundOrderNo;
    
    /**
     * 订单类型：1-电池滞纳金，2-保险支付，3-换电套餐
     */
    private Integer orderType;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
}
