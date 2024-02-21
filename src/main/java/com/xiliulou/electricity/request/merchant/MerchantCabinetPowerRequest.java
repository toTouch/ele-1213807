package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 小程序-电费请求参数
 * @date 2024/2/20 19:05:20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantCabinetPowerRequest {
    
    private Long merchantId;
    
    private Long placeId;
    
    private Long cabinetId;
    
    private Long startTime;
    
    private Long endTime;
}
