package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 柜机电费
 * @date 2024/2/26 03:20:07
 */
@Data
public class MerchantProCabinetPowerVO {
    
    private Long cabinetId;
    
    private String cabinetName;
    
    private BigDecimal todayPower;
    
    private BigDecimal todayCharge;
    
    private BigDecimal thisMonthPower;
    
    private BigDecimal thisMonthCharge;
    
    private BigDecimal thisYearPower;
    
    private BigDecimal thisYearCharge;
    
    /**
     * 柜机创建时间，用于列表排序
     */
    private Long time;
}
