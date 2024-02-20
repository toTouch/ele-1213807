package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 今日/昨日电量和电费
 * @date 2024/2/20 19:09:17
 */
@Data
public class MerchantCabinetDayPowerVO {
    
    private Double sumPower;
    
    private Double sumCharge;
    
}
