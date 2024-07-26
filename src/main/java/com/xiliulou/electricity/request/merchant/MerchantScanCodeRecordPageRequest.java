package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantScanCodeRecordPageRequest {
    
    private Long size;
    
    private Long offset;
    
    private Long merchantId;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private String phone;
    
    /**
     * 扫码时间
     */
    private Long scanTimeStart;
    
    private Long scanTimeEnd;
    
    /**
     * 购买时间
     */
    private Long buyTimeStart;
    
    private Long buyTimeEnd;
    
    
    private List<Long> uids;
    
    private List<String> orderIdList;
}
