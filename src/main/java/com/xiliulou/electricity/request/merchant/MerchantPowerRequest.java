package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 场地电费请求
 * @date 2024/2/24 18:15:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPowerRequest {
    
    private long size;
    
    private long offset;
    
    private String monthDate;
}
