package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序-商户 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantPowerVO {
    
    private MerchantPowerRspVO todayPower;
    
    private MerchantPowerRspVO yesterdayPower;
    
    private MerchantPowerRspVO thisMonthPower;
    
    private MerchantPowerRspVO lastMonthPower;
    
    private MerchantPowerRspVO totalPower;
    
    private List<MerchantCabinetPowerVO> cabinetPowerList;
}
