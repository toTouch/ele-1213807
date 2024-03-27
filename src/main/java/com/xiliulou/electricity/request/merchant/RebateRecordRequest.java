package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RebateRecordRequest {
    
    private Long size;
    private Long offset;
    private Integer tenantId;
    
    private Long uid;
    
    private String orderId;
    
    private Integer type;
    
    private Long franchiseeId;
    
    private Long merchantId;
    
    /**
     * 渠道员
     */
    private Long channeler;
    
    /**
     * 返现时间
     */
    private Long beginTime;
    
    /**
     * 结算时间
     */
    private Long endTime;
}
