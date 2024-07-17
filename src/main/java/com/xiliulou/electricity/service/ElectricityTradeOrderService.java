package com.xiliulou.electricity.service;


import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.pay.base.request.BaseOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ElectricityTradeOrderService {
    
    /**
     * 租车套餐购买回调
     *
     * @param callBackResource
     */
    Pair<Boolean, Object> notifyCarRenalPackageOrder(BaseOrderCallBackResource callBackResource);
    
    //通用生成订单，调起支付
    WechatJsapiOrderResultDTO commonCreateTradeOrderAndGetPayParams(CommonPayOrder commonPayOrder, WechatPayParamsDetails wechatPayParamsDetails, String openId,
            HttpServletRequest request) throws WechatPayException;
    
    
    //月卡回调
    Pair<Boolean, Object> notifyMemberOrder(BaseOrderCallBackResource callBackResource);
    
    
    //押金支付回调
    Pair<Boolean, Object> notifyDepositOrder(BaseOrderCallBackResource callBackResource);
    
    //电池服务费支付回调
    Pair<Boolean, Object> notifyBatteryServiceFeeOrder(BaseOrderCallBackResource callBackResource);
    
    //租车押金支付回调
    Pair<Boolean, Object> notifyRentCarDepositOrder(BaseOrderCallBackResource callBackResource);
    
    //租车月卡回调
    Pair<Boolean, Object> notifyRentCarMemberOrder(BaseOrderCallBackResource callBackResource);
    
    //保险回调
    Pair<Boolean, Object> notifyInsuranceOrder(BaseOrderCallBackResource callBackResource);
    
    /**
     * 云豆充值回调
     *
     * @param callBackResource
     * @return
     */
    Pair<Boolean, Object> notifyCloudBeanRechargeOrder(BaseOrderCallBackResource callBackResource);
    
    ElectricityTradeOrder selectTradeOrderByTradeOrderNo(String outTradeNo);
    
    
    ElectricityTradeOrder selectTradeOrderByOrderId(String orderId);
    
    void insert(ElectricityTradeOrder electricityTradeOrder);
    
    List<ElectricityTradeOrder> selectTradeOrderByParentOrderId(Long parentOrderId);
    
    Integer updateElectricityTradeOrderById(ElectricityTradeOrder electricityTradeOrder);
    
    ElectricityTradeOrder selectTradeOrderByOrderIdV2(String orderId);
}
