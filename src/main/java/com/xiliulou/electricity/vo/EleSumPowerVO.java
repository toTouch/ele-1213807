package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 总电量/总电费
 * @date 2024/2/20 20:18:26
 */
@Data
public class EleSumPowerVO {
    
    private BigDecimal sumPower;
    
    private BigDecimal sumCharge;
}
