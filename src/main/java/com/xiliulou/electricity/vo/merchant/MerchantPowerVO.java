package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序统计 电量/电费
 * @date 2024/2/22 15:55:20
 */
@Data
public class MerchantPowerVO {
    
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
    
    List<MerchantCabinetPowerVO> cabinetPowerList;
}
