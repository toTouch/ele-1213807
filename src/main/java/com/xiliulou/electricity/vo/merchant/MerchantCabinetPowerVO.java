package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 柜机电量和电费
 * @date 2024/2/20 19:09:17
 */
@Data
public class MerchantCabinetPowerVO {
    
    private Double todayPower;
    
    private Double todayCharge;
    
    private Double thisMonthPower;
    
    private Double thisMonthCharge;
    
    private Double thisYearPower;
    
    private Double thisYearCharge;
    
}
