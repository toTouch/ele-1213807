package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetail;

import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序月度电费详情
 * @date 2024/2/20 11:09:59
 */
public interface MerchantCabinetPowerMonthDetailProService {
    
    Long queryLatestReportTime(Long cabinetId, List<String> monthList);
    
    List<MerchantCabinetPowerMonthDetail> listByMonth(Long cabinetId, List<String> monthList);
}
