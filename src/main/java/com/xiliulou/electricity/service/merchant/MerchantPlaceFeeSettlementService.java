package com.xiliulou.electricity.service.merchant;

import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName : MerchantPlaceFeeSettlementService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
public interface MerchantPlaceFeeSettlementService {
    void export(String date, HttpServletResponse response);
}
