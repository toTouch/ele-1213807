package com.xiliulou.electricity.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName: FreeDepositOrderRequest
 * @description:
 * @author: renhang
 * @create: 2024-08-21 19:17
 */
@Data
public class FreeDepositOrderRequest {
    
    private Long franchiseeId;
    
    private String phoneNumber;
    
    private String idCard;
    
    private String realName;
    
    private Integer model;
    
    private Integer tenantId;
    
    private String freeDepositOrderId;
    
    private BigDecimal payAmount;
}
