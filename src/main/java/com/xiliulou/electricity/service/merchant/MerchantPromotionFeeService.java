package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;

/**
 * @ClassName : MerchantPromotionFeeService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
public interface MerchantPromotionFeeService {
    R queryMerchantAvailableWithdrawAmount(Long uid);
    
    R queryMerchantPromotionFeeIncome(Integer type,Long uid);
    
    R queryMerchantPromotionScanCode(Integer type,Long uid);
    
}
