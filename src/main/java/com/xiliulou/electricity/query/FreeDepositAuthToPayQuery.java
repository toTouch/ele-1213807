package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName: FreeDepositAuthToPayQuery
 * @description: 免押代扣
 * @author: renhang
 * @create: 2024-08-22 17:24
 */
@Data
@Builder
public class FreeDepositAuthToPayQuery {
    
    
    private Integer tenantId;
    
    /**
     * 免押渠道
     */
    private Integer channel;
    
    /**
     * 免押订单号
     */
    private String orderId;
    
    /**
     * uid
     */
    private Long uid;
    
    /**
     * 订单标题
     */
    private String subject;
    
    /**
     * 代扣金额
     */
    private BigDecimal payTransAmt;
    
    /**
     * 授权码
     */
    private String authNo;
    
    /**
     * 代扣回调地址
     */
    private String notifyUrl;
    
}
