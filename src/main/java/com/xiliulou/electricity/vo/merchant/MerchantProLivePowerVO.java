package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantProLivePowerVO {
    
    private Long placeId;
    
    private Long eid;
    
    private BigDecimal power;
    
    private BigDecimal charge;
    
    private Long startTime;
    
    private Long endTime;
}
