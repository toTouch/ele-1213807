package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author HeYafeng
 * @description 商户场地绑定请求
 * @date 2024/2/26 14:03:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPlaceConditionRequest {
    
    private Long merchantId;
    
    private Set<Long> placeIds;
    
    private Integer status;
    
    private Long startTime;
    
    private Long endTime;
    
}
