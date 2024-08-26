package com.xiliulou.electricity.service.handler.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.constant.FreeDepositConstant;
import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.handler.AbstractCommonFreeDeposit;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzAuthToPayRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzDepositUnfreezeRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    
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
            log.error("Pxz ERROR! freeDepositOrderQuery fail!  orderId={}", orderId, e);
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
            log.error("Pxz ERROR! unfreeDepositOrder fail! uid={},orderId={}", uid, orderId, e);
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
    public Object freeDepositNotified(Integer business, Map<String, Object> params) {
        
        Map<String, Object> map = new HashMap<>(1);
        
        // 免押 和 解冻，拍小租根据返回状态区分
        if (Objects.equals(business, FreeBusinessTypeEnum.FREE.getCode())) {
            return freeOrUnFreeHandler(params, map);
        }
        
        // 代扣
        if (Objects.equals(business, FreeBusinessTypeEnum.AUTH_PAY.getCode())) {
            return authPayHandler(params, map);
        }
        
        throw new CustomBusinessException("拍小组回调异常");
    }
    
    private Map<String, Object> authPayHandler(Map<String, Object> params, Map<String, Object> map) {
        String orderId = (String) params.get("orderId");
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
        
        // 如果没有订单则确认成功
        if (Objects.isNull(freeDepositOrder)) {
            log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
            map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
            return map;
        }
        
        Integer orderStatus = (Integer) params.get("orderStatus");
        if (Objects.equals(orderStatus, FreeDepositConstant.AUTH_PXZ_SUCCESS_RECEIVE)) {
            // 成功更新状态
            handlerAuthPaySuccess(freeDepositOrder);
            map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
            return map;
        }
        map.put("respCode", FreeDepositConstant.AUTH_PXZ_FAIL_RSP);
        return map;
    }
    
    private Map<String, Object> freeOrUnFreeHandler(Map<String, Object> params, Map<String, Object> map) {
        String orderId = (String) params.get("transId");
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
        // 如果没有订单则确认成功
        if (Objects.isNull(freeDepositOrder)) {
            log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
            map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
            return map;
        }
        
        Integer orderStatus = (Integer) params.get("authStatus");
        // 上一个状态是待冻结，本次状态是已冻结==免押
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_PENDING_FREEZE) && Objects.equals(orderStatus, FreeDepositOrder.AUTH_FROZEN)) {
            // 免押成功 修改状态逻辑
            // handlerUnfree(freeDepositOrder);
            map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
            return map;
        }
        
        // 上一个状态是冻结，本次是解冻==解冻
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN) && Objects.equals(orderStatus, FreeDepositOrder.AUTH_UN_FROZEN)) {
            // 解冻成功 修改状态逻辑
            // handlerFreeDepositSuccess(channel, freeDepositOrder);
            map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
            return map;
        }
        map.put("respCode", FreeDepositConstant.AUTH_PXZ_FAIL_RSP);
        // todo 更新为返回的状态
        return map;
    }
}
