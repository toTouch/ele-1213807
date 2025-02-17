package com.xiliulou.electricity.service.merchant;

import com.xiliulou.pay.weixinv3.dto.WechatTransferOrderCallBackResource;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 17:46
 */
public interface MerchantWithdrawApplicationBizService {

    void handleSendMerchantWithdrawProcess(Integer tenantId);

    void handleQueryWithdrawResult(Integer tenantId);

    void handleNotify(WechatTransferOrderCallBackResource callBackResource);
}
