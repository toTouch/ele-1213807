package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 折线图电费
 * @date 2024/2/26 03:20:07
 */
@Data
public class MerchantProPowerChargeLineDataVO {
    
    private String monthDate;
    
    private BigDecimal charge;
}
