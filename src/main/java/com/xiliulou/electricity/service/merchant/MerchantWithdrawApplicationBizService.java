package com.xiliulou.electricity.service.merchant;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 17:46
 */
public interface MerchantWithdrawApplicationBizService {

    void handleSendMerchantWithdrawProcess(Integer tenantId);

    void handleQueryWithdrawResult(Integer tenantId);
}
