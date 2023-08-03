package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.*;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;

public interface UnionTradeOrderService {

    //通用生成订单，调起支付
    WechatJsapiOrderResultDTO unionCreateTradeOrderAndGetPayParams(UnionPayOrder unionPayOrder,
                                                                    ElectricityPayParams electricityPayParams,
                                                                    String openId,
                                                                    HttpServletRequest request) throws WechatPayException;


    //集成支付回调
    Pair<Boolean, Object> notifyIntegratedPayment(WechatJsapiOrderCallBackResource callBackResource);

    Pair<Boolean, Object> manageInsuranceOrder(String orderNo, Integer orderStatus);

    Pair<Boolean, Object> manageMemberCardOrder(String orderNo, Integer orderStatus);

    Pair<Boolean, Object> manageDepositOrder(String orderNo, Integer orderStatus);


    UnionTradeOrder selectTradeOrderByOrderId(String orderId);

    UnionTradeOrder selectTradeOrderById(Long id);

    Pair<Boolean, Object> notifyMembercardInsurance(WechatJsapiOrderCallBackResource callBackResource);
}
