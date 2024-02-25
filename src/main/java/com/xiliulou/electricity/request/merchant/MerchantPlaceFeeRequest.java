package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/25 15:55
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPlaceFeeRequest {
    private Long merchantId;
    
    private Long placeId;
    
    private Long cabinetId;
    
    private Long startTime;
    
    private Long endTime;
    
    private String month;
}
