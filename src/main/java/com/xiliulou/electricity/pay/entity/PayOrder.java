package com.xiliulou.electricity.pay.entity;

import lombok.Data;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-02 11:29
 **/
@Data
public class PayOrder {
    //appId
    private String appId;

    private String appSecret;
    //商户id
    private String mchId;
    private String paternerKey;
    //商品描述
    private String body;
    //额外参数
    private String attach;
    //商户号
    private String outTradeNo;

    //支付金额(单位:分)
    private Long totalFee;
    //用户openId
    private String openId;
    //订单过期时间
    private String timeExpire;
    //支付渠道
    private String channelId;
    //ip
    private String spbillCreateIp;

    //微信小程序支付
    public static final String CHANNEL_ID_WX_PRO = "WX_PRO";
}
