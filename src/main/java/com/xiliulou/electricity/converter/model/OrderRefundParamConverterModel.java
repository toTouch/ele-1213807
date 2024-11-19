/**
 *  Create date: 2024/7/18
 */

package com.xiliulou.electricity.converter.model;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.enums.RefundPayOptTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class OrderRefundParamConverterModel {
    
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
     * 退款金额 (单位：元)
     */
    private BigDecimal refund;
    
    /**
     * 原支付交易的订单总金额(单位：分)，仅微信使用。
     */
    private Integer total;
    
    /**
     * CNY：人民币，境内商户号仅支持人民币。默认CNY
     */
    private String currency;
    
    
    /**
     * 支付配置信息
     */
    private BasePayConfig payConfig;
    
    
    /**
     * 退款类型
     * @see RefundPayOptTypeEnum
     */
    private String refundType;
    
    
    
    private Integer tenantId;
    
    private Long franchiseeId;
}
