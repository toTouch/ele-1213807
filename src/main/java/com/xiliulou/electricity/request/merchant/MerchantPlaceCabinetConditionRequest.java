package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author HeYafeng
 * @description
 * @date 2024/2/22 19:24:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPlaceCabinetConditionRequest {
    
    private Long placeId;
    
    private Set<Long> cabinetIds;
    
    private Integer status;
    
    private Long startTime;
    
    private Long endTime;
    
    private Integer powerSettleStatus;
}
