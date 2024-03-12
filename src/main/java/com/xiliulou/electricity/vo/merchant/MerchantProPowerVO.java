package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 小程序-商户 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantProPowerVO {
    
    private BigDecimal todayPower;
    
    private BigDecimal todayCharge;
    
    private BigDecimal yesterdayPower;
    
    private BigDecimal yesterdayCharge;
    
    private BigDecimal thisMonthPower;
    
    private BigDecimal thisMonthCharge;
    
    private BigDecimal lastMonthPower;
    
    private BigDecimal lastMonthCharge;
    
    private BigDecimal totalPower;
    
    private BigDecimal totalCharge;
    
}
