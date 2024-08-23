package com.xiliulou.electricity.service.handler.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.bo.UnFreeDepositOrderBO;
import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.service.handler.AbstractCommonFreeDeposit;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
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
        
        Triple<Boolean, String, Object> triple = PxzResultCheck(callPxzRsp, orderId);
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
            log.error("Pxz ERROR! freeDepositOrderQuery fail!  orderId={}", orderId, e);
            return null;
        }
        
        Triple<Boolean, String, Object> triple = PxzResultCheck(pxzQueryOrderRsp, orderId);
        if (!triple.getLeft()) {
            return null;
        }
        
        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        return BeanUtil.copyProperties(queryOrderRspData, FreeDepositOrderStatusBO.class);
    }
    
    @Override
    public Triple<Boolean, String, Object> unFreezeDeposit(FreeDepositOrderStatusQuery query) {
        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzUnfreezeDepositCommonRsp = null;
        Long uid = query.getUid();
        String orderId = query.getOrderId();
        try {
            pxzUnfreezeDepositCommonRsp = pxzDepositService.unfreezeDeposit(buildUnFreeDepositOrderPxzRequest(query));
        } catch (Exception e) {
            log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! uid={},orderId={}", uid, orderId, e);
            return Triple.of(false, "100401", "免押解冻调用失败！");
        }
        
        Triple<Boolean, String, Object> triple = PxzResultCheck(pxzUnfreezeDepositCommonRsp, orderId);
        if (!triple.getLeft()) {
            return triple;
        }
        
        return Triple.of(true, null, BeanUtil.copyProperties(pxzUnfreezeDepositCommonRsp.getData(), UnFreeDepositOrderBO.class));
    }
}
