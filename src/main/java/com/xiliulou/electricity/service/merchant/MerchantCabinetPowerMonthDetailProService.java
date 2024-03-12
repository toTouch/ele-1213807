package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetailPro;

import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序月度电费详情
 * @date 2024/2/20 11:09:59
 */
public interface MerchantCabinetPowerMonthDetailProService {
    
    List<MerchantCabinetPowerMonthDetailPro> listByMonth(Long cabinetId, List<String> monthList, Long merchantId);
}
