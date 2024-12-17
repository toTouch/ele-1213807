package com.xiliulou.electricity.service;


import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.pay.base.dto.BasePayOrderCreateDTO;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.base.request.BaseOrderCallBackResource;
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
    
    
    /**
     * 通用生成订单，调起支付 V2
     *
     * @param commonPayOrder
     * @param payConfig
     * @param openId
     * @param request
     * @return
     * @author caobotao.cbt
     * @date 2024/7/18 19:20
     */
    BasePayOrderCreateDTO commonCreateTradeOrderAndGetPayParamsV2(CommonPayOrder commonPayOrder, BasePayConfig payConfig, String openId, HttpServletRequest request)
            throws PayException;
    
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
    
    List<ElectricityTradeOrder> listByChannelOrderNoList(List<String> transactionIdList, Integer status, Long endTime);
    
    List<Integer> existByTenantIdList(List<Integer> tenantIdList, long startTime, long endTime);
}
