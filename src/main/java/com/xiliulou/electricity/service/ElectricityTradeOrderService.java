package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.pay.weixin.entity.WeiXinPayNotify;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;

public interface ElectricityTradeOrderService {


    Pair<Boolean, Object> createTradeOrderAndGetPayParams(ElectricityMemberCardOrder electricityMemberCardOrder,
                                                          ElectricityPayParams electricityPayParams,
                                                          String openId,
                                                          HttpServletRequest request);

    Pair<Boolean, Object> notifyMemberOrder(WeiXinPayNotify weiXinPayNotify);
}
