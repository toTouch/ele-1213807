package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;

/**
 * @ClassName : MerchantPromotionFeeService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
public interface MerchantPromotionFeeService {
    R queryPromotionFeeHome(Integer type,Long uid);
}
