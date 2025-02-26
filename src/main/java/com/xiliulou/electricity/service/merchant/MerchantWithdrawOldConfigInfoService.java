package com.xiliulou.electricity.service.merchant;


import com.xiliulou.electricity.entity.merchant.Merchant;

/**
 * 商户提现使用旧配置信息表(TMerchantWithdrawOldConfigInfo)表服务接口
 *
 * @author maxiaodong
 * @since 2025-02-13 17:43:59
 */
public interface MerchantWithdrawOldConfigInfoService {

    boolean existsMerchantOldWithdrawConfigInfo(Integer tenantId, Long franchiseeId);
}
