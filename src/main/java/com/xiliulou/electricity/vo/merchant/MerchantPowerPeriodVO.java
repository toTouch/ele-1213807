package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 时间段的 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantPowerPeriodVO {
    
    private BigDecimal power;
    
    private BigDecimal charge;
}
