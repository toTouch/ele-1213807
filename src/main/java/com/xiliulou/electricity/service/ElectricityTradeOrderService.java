package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.pay.weixin.entity.WeiXinPayNotify;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;

public interface ElectricityTradeOrderService {


    Pair<Boolean, Object> createTradeOrderAndGetPayParams(ElectricityMemberCardOrder electricityMemberCardOrder,
                                                          ElectricityPayParams electricityPayParams,
                                                          String openId,
                                                          HttpServletRequest request);


    Pair<Boolean, Object> notifyMemberOrder(WeiXinPayNotify weiXinPayNotify);

    //通用生成订单，调起支付
    Pair<Boolean, Object> commonCreateTradeOrderAndGetPayParams(CommonPayOrder commonPayOrder,
                                                                ElectricityPayParams electricityPayParams,
                                                                String openId,
                                                                HttpServletRequest request);

    //押金支付回调
    Pair<Boolean, Object> notifyDepositOrder(WeiXinPayNotify weiXinPayNotify);

    ElectricityTradeOrder selectTradeOrderByTradeOrderNo(String outTradeNo);
}
