package com.xiliulou.electricity.service.handler.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.AuthPayStatusBO;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositAuthToPayStatusQuery;
import com.xiliulou.electricity.query.FreeDepositCancelAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.electricity.service.handler.AbstractCommonFreeDeposit;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzAuthToPayOrderQueryRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzAuthToPayRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzDepositUnfreezeRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName: PxzBaseFreeDepositOrderServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
@Service("pxzFreeDepositOrderServiceImpl")
@Slf4j
public class PxzBaseFreeDepositOrderServiceImpl extends AbstractCommonFreeDeposit implements BaseFreeDepositService {
    
    @Resource
    PxzDepositService pxzDepositService;
    
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        String orderId = request.getFreeDepositOrderId();
        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(buildFreeDepositOrderPxzRequest(request));
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        Triple<Boolean, String, Object> triple = pxzResultCheck(callPxzRsp, orderId);
        if (!triple.getLeft()) {
            return triple;
        }
        
        FreeDepositOrderDTO dto = FreeDepositOrderDTO.builder().channel(FreeDepositChannelEnum.PXZ.getChannel()).data(callPxzRsp.getData()).build();
        return Triple.of(true, null, dto);
    }
    
    @Override
    public FreeDepositOrderStatusBO queryFreeDepositOrderStatus(FreeDepositOrderStatusQuery query) {
        
        String orderId = query.getOrderId();
        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(buildQueryFreeDepositOrderStatusPxzRequest(query));
        } catch (PxzFreeDepositException e) {
            log.error("Pxz ERROR! queryFreeDepositOrderStatus fail!  orderId={}", orderId, e);
            return null;
        }
        
        Triple<Boolean, String, Object> triple = pxzResultCheck(pxzQueryOrderRsp, orderId);
        if (!triple.getLeft()) {
            return null;
        }
        
        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        return BeanUtil.copyProperties(queryOrderRspData, FreeDepositOrderStatusBO.class);
    }
    
    @Override
    public Triple<Boolean, String, Object> unFreezeDeposit(UnFreeDepositOrderQuery query) {
        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzUnfreezeDepositCommonRsp = null;
        Long uid = query.getUid();
        String orderId = query.getOrderId();
        try {
            pxzUnfreezeDepositCommonRsp = pxzDepositService.unfreezeDeposit(buildUnFreeDepositOrderPxzRequest(query));
        } catch (Exception e) {
            log.error("Pxz ERROR! unFreezeDeposit fail! uid={},orderId={}", uid, orderId, e);
            return Triple.of(false, "100401", "免押解冻调用失败！");
        }
        
        Triple<Boolean, String, Object> triple = pxzResultCheck(pxzUnfreezeDepositCommonRsp, orderId);
        if (!triple.getLeft()) {
            return triple;
        }
        
        return Triple.of(true, null, "解冻中，请稍后");
    }
    
    @Override
    public Triple<Boolean, String, Object> authToPay(FreeDepositAuthToPayQuery query) {
        PxzCommonRsp<PxzAuthToPayRsp> authToPayRsp = null;
        Long uid = query.getUid();
        String orderId = query.getOrderId();
        try {
            authToPayRsp = pxzDepositService.authToPay(buildAuthPxzRequest(query));
        } catch (Exception e) {
            log.error("Pxz ERROR! authToPay fail! uid={},orderId={}", uid, orderId, e);
            return Triple.of(false, "100401", "免押代扣调用失败！");
        }
        
        Triple<Boolean, String, Object> triple = pxzResultCheck(authToPayRsp, orderId);
        if (!triple.getLeft()) {
            return triple;
        }
        
        return Triple.of(true, null, "免押代扣中，请稍后");
    }
    
    @Override
    public AuthPayStatusBO queryAuthToPayStatus(FreeDepositAuthToPayStatusQuery query) {
        PxzCommonRsp<PxzAuthToPayOrderQueryRsp> result = null;
        Long uid = query.getUid();
        String orderId = query.getOrderId();
        try {
            result = pxzDepositService.authToPayOrderQuery(buildAuthPxzStatusRequest(query));
        } catch (Exception e) {
            log.error("Pxz ERROR! queryAuthToPayStatus fail! uid={},orderId={}", uid, orderId, e);
            return null;
        }
        
        Triple<Boolean, String, Object> triple = pxzResultCheck(result, orderId);
        if (!triple.getLeft()) {
            return null;
        }
        
        return AuthPayStatusBO.builder().orderId(result.getData().getOrderId()).orderStatus(result.getData().getOrderStatus()).build();
    }
    
    @Override
    public Boolean cancelAuthPay(FreeDepositCancelAuthToPayQuery query) {
        PxzCommonRsp<Boolean> result = null;
        Long uid = query.getUid();
        String orderId = query.getAuthPayOrderId();
        try {
            result = pxzDepositService.cancelAuthToPay(buildCancelAuthPayPxzRequest(query));
        } catch (Exception e) {
            log.error("Pxz ERROR! cancelAuthPay fail! uid={},orderId={}", uid, orderId, e);
            return false;
        }
        Triple<Boolean, String, Object> triple = pxzResultCheck(result, orderId);
        if (!triple.getLeft()) {
            return false;
        }
        log.info("PXZ INFO! cancelAuthPay.result is {}", JsonUtil.toJson(result));
        
        return result.getData();
    }
    
}
