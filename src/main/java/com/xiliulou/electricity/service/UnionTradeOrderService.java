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


    //押金保险混合回调
    Pair<Boolean, Object> notifyUnionDepositAndInsurance(WechatJsapiOrderCallBackResource callBackResource);


    UnionTradeOrder selectTradeOrderByOrderId(String orderId);

    UnionTradeOrder selectTradeOrderById(Long id);

}
