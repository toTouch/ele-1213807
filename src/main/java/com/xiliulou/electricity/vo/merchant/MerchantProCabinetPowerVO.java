package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 柜机电费
 * @date 2024/2/26 03:20:07
 */
@Data
public class MerchantProCabinetPowerVO {
    
    private Long cabinetId;
    
    private String cabinetName;
    
    private Double todayPower;
    
    private Double todayCharge;
    
    private Double thisMonthPower;
    
    private Double thisMonthCharge;
    
    private Double thisYearPower;
    
    private Double thisYearCharge;
    
    /**
     * 最新上报数据的时间，用于对柜机进行排序
     */
    private Long latestTime;
}
