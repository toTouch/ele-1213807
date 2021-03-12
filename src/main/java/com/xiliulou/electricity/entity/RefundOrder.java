package com.xiliulou.electricity.entity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: LXC
 * @create: 2021-02-26 09:45
 **/
@Data
@Builder
public class RefundOrder {
    /**
     * 退款单号
     */
    private String refundOrderNo;
    /**
     * 支付单号
     */
    private String orderId;
    /**
     * 支付金额,单位元
     */
    private BigDecimal payAmount;
    /**
     * 退款金额,单位元
     */
    private BigDecimal refundAmount;
    //额外参数
    private String attach;


}
