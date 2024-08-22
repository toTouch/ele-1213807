package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName: FreeDepositOrderRequest
 * @description:
 * @author: renhang
 * @create: 2024-08-21 19:17
 */
@Data
@Builder
public class FreeDepositOrderRequest {
    
    private Long franchiseeId;
    
    /**
     * 手机号
     */
    private String phoneNumber;
    
    /**
     * 身份证
     */
    private String idCard;
    
    /**
     * 名
     */
    private String realName;
    
    private Integer model;
    
    private Integer tenantId;
    
    /**
     * 免押订单号
     */
    private String freeDepositOrderId;
    
    /**
     * 免押金额
     */
    private BigDecimal payAmount;
    
    private Long uid;
    
    
    /**
     * 免押标题
     */
    private String subject;
    
    /**
     * 回调地址，免押成功通知
     */
    private String callbackUrl;
}
