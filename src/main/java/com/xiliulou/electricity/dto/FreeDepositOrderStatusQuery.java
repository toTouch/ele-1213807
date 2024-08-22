package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: FreeDepositOrderStatusQuery
 * @description:
 * @author: renhang
 * @create: 2024-08-22 17:24
 */
@Data
@Builder
public class FreeDepositOrderStatusQuery {
    
    private Long franchiseeId;
    
    
    private Integer tenantId;
    
    /**
     * 免押渠道
     */
    private Integer channel;
    
    /**
     * 免押订单号
     */
    private String orderId;
    
    
    private Long uid;
    
}
