package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-20-16:05
 */
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
    
    private List<Long> franchiseeIds;
    
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
