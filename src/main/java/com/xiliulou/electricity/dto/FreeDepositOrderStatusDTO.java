package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: FreeDepositOrderStatusDTO
 * @description:
 * @author: renhang
 * @create: 2024-08-22 10:55
 */
@Data
@Builder
public class FreeDepositOrderStatusDTO {
    
    /**
     * 免押订单号
     */
    private String orderId;
    
    private Long uid;
    
    /**
     * 免押渠道
     */
    private Integer channel;
    
    private Integer tenantId;
}
