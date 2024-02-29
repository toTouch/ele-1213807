package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.vo.merchant.MerchantPowerPeriodVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerDetailVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 商户电费业务（小程序端）
 * @date 2024/2/26 03:33:07
 */
public interface MerchantCabinetPowerMonthRecordProService {
    
    MerchantPowerPeriodVO sumMonthPower(List<Long> cabinetIds, List<String> monthDateList);
}
