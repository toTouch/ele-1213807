package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.query.merchant.MerchantPowerDetailQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthDetailVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 电费月度详情
 * @date 2024/2/20 11:09:59
 */
public interface MerchantCabinetPowerMonthDetailService {
    
    List<MerchantCabinetPowerMonthDetailVO> listByTenantId(MerchantPowerDetailQueryModel queryModel);
}
