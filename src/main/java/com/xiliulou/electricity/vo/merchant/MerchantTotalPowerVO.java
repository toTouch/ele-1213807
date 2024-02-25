package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 累计电量/电费、本月电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantTotalPowerVO {
    
    private Double totalPower;
    
    private Double totalCharge;
    
    private Double thisMonthPower;
    
    private Double thisMonthCharge;
}
