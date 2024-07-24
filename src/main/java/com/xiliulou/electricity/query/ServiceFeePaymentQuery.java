package com.xiliulou.electricity.query;

import com.xiliulou.pay.base.enums.ChannelEnum;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-24-9:59
 */
@Data
public class ServiceFeePaymentQuery {
    /**
     * 支付渠道 WECHAT-微信支付,ALIPAY-支付宝
     */
    private String paymentChannel = ChannelEnum.WECHAT.getCode();
}
