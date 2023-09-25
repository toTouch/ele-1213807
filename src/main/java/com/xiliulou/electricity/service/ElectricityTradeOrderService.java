package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ElectricityTradeOrderService {

    /**
     * 租车套餐购买回调
     * @param callBackResource
     */
    Pair<Boolean, Object> notifyCarRenalPackageOrder(WechatJsapiOrderCallBackResource callBackResource);

    //通用生成订单，调起支付
    WechatJsapiOrderResultDTO commonCreateTradeOrderAndGetPayParams(CommonPayOrder commonPayOrder,
                                                                    ElectricityPayParams electricityPayParams,
                                                                    String openId,
                                                                    HttpServletRequest request) throws WechatPayException;


    //月卡回调
    Pair<Boolean, Object> notifyMemberOrder(WechatJsapiOrderCallBackResource callBackResource);


    //押金支付回调
    Pair<Boolean, Object> notifyDepositOrder(WechatJsapiOrderCallBackResource callBackResource);

    //电池服务费支付回调
    Pair<Boolean, Object> notifyBatteryServiceFeeOrder(WechatJsapiOrderCallBackResource callBackResource);

    //租车押金支付回调
    Pair<Boolean, Object> notifyRentCarDepositOrder(WechatJsapiOrderCallBackResource callBackResource);

    //租车月卡回调
    Pair<Boolean, Object> notifyRentCarMemberOrder(WechatJsapiOrderCallBackResource callBackResource);

    //保险回调
    Pair<Boolean, Object> notifyInsuranceOrder(WechatJsapiOrderCallBackResource callBackResource);

    /**
     * 云豆充值回调
     * @param callBackResource
     * @return
     */
    Pair<Boolean, Object> notifyCloudBeanRechargeOrder(WechatJsapiOrderCallBackResource callBackResource);

    Pair<Boolean, Object> notifyPurchaseEnterprisePackageOrder(WechatJsapiOrderCallBackResource callBackResource);

    ElectricityTradeOrder selectTradeOrderByTradeOrderNo(String outTradeNo);


    ElectricityTradeOrder selectTradeOrderByOrderId(String orderId);

    void insert(ElectricityTradeOrder electricityTradeOrder);

    List<ElectricityTradeOrder> selectTradeOrderByParentOrderId(Long parentOrderId);

    Integer updateElectricityTradeOrderById(ElectricityTradeOrder electricityTradeOrder);

}
