package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;


/**
 * @ClassName: FreeDepositAuthToPayQuery
 * @description: 免押代扣
 * @author: renhang
 * @create: 2024-08-22 17:24
 */
@Data
@Builder
public class FreeDepositAuthToPayStatusQuery {
    
    
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
     * 代扣订单号(代扣记录表中的)
     */
    private String authPayOrderId;
    
    /**
     * uid
     */
    private Long uid;
    
    
    /**
     * 授权码
     */
    private String authNo;
    
    
}
