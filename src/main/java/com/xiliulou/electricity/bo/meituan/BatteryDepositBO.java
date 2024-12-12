package com.xiliulou.electricity.bo.meituan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @date 2024/12/11 09:34:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryDepositBO {
    
    private Long franchiseeId;
    
    /**
     * 套餐押金
     */
    private BigDecimal deposit;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 是否免押 0--是 1--否
     */
    private Integer freeDeposit;
}
