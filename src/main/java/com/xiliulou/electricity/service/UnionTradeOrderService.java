package com.xiliulou.electricity.service;


import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.pay.base.dto.BasePayOrderCreateDTO;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.base.request.BaseOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;

public interface UnionTradeOrderService {

    
    
    BasePayOrderCreateDTO unionCreateTradeOrderAndGetPayParams(UnionPayOrder unionPayOrder, BasePayConfig payParamConfig, String openId, HttpServletRequest request)
            throws PayException;


    //集成支付回调
    Pair<Boolean, Object> notifyIntegratedPayment(BaseOrderCallBackResource callBackResource);

    Pair<Boolean, Object> manageInsuranceOrder(String orderNo, Integer orderStatus, UserInfo userInfo);

    Pair<Boolean, Object> manageMemberCardOrder(String orderNo, Integer orderStatus, UserInfo userInfo);

    Pair<Boolean, Object> manageMemberCardOrderV2(String orderNo, Integer orderStatus, UserInfo userInfo);
    
    Pair<Boolean, Object> manageEnterpriseMemberCardOrder(String orderNo, Integer orderStatus);

    Pair<Boolean, Object> manageDepositOrder(String orderNo, Integer orderStatus, UserInfo userInfo);

    UnionTradeOrder selectTradeOrderByOrderId(String orderId);

    UnionTradeOrder selectTradeOrderById(Long id);

    Pair<Boolean, Object>  notifyMembercardInsurance(BaseOrderCallBackResource callBackResource);

    Pair<Boolean, Object> notifyServiceFee(BaseOrderCallBackResource callBackResource);
    
}
