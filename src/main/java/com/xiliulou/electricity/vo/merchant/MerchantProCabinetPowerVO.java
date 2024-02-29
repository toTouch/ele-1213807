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
     * 柜机创建时间，用于列表排序
     */
    private Long time;
}
