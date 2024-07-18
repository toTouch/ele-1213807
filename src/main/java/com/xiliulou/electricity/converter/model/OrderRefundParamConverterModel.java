/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/18
 */

package com.xiliulou.electricity.converter.model;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.metrics.stats.Total;

import java.math.BigDecimal;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/18 09:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRefundParamConverterModel<T extends BasePayConfig> {
    
    /**
     * 商户系统内部的退款单号
     */
    private String refundId;
    
    /**
     * 原支付交易对应的商户订单号
     */
    private String orderId;
    
    /**
     * 原因
     */
    private String reason;
    
    /**
     * 回调地址
     */
    private String notifyUrl;
    
    /**
     * 退款金额 (单位：元)
     */
    private BigDecimal refund;
    
    /**
     * 原支付交易的订单总金额(单位：元)。
     */
    private BigDecimal total;
    
    /**
     * CNY：人民币，境内商户号仅支持人民币。默认CNY
     */
    private String currency;
    
    
    /**
     * 支付配置信息
     */
    private PayParamsBizDetails<T> payParamsBizDetails;
    
}
