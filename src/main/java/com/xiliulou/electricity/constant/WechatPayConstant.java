package com.xiliulou.electricity.constant;

/**
 * @author: eclair
 * @Date: 2021/3/19 16:17
 * @Description:
 */
public interface WechatPayConstant {
	//微信支付回调幂等id
	String PAY_ORDER_ID_CALL_BACK = "pay_call_back:";
	//微信退款回调幂等id
	String REFUND_ORDER_ID_CALL_BACK = "refund_call_back:";
}
