package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 小程序-商户 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantProPowerVO {
    
    private Double todayPower;
    
    private Double todayCharge;
    
    private Double yesterdayPower;
    
    private Double yesterdayCharge;
    
    private Double thisMonthPower;
    
    private Double thisMonthCharge;
    
    private Double lastMonthPower;
    
    private Double lastMonthCharge;
    
    private Double totalPower;
    
    private Double totalCharge;
    
}
