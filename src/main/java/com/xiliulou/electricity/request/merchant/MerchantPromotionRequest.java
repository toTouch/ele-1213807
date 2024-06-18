package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 商户推广费请求
 * @date 2024/2/24 11:02:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPromotionRequest {
    
    private long size;
    
    private long offset;
    
    private String monthDate;
    
    private List<Long> franchiseeIds;
}
