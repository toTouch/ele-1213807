package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: UnFreeDepositOrderQuery
 * @description: 免押解冻
 * @author: renhang
 * @create: 2024-08-22 17:24
 */
@Data
@Builder
public class UnFreeDepositOrderQuery {
    
    
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
     * 主题
     */
    private String subject;
    
    /**
     * 押金金额
     */
    private String amount;
    
}
