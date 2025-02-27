package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/11 22:36
 * @desc 商户提现流程VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantWithdrawConfirmReceiptVO {
    /**
     * 【商户号】商户号，由微信支付生成并下发
     */
    private String mchId;

    /**
     * 【商户AppID】商户绑定的AppID（企业号corpid即为此AppID），由微信生成，可在公众号后台查看
     */
    private String appId;

    /**
     * 【【跳转页面的package信息】商家转账付款单跳转收款页package信息,商家转账付款单受理成功时返回给商户
     */
    private String packageInfo;
}
