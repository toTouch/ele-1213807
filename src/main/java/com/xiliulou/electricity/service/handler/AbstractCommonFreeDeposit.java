package com.xiliulou.electricity.service.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.pay.deposit.fengyun.constant.FyConstants;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.AuthPayVars;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyAuthPayRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositUnfreezeRequest;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @ClassName: AbstractCommonFreeDeposit
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:24
 */

@Slf4j
public abstract class AbstractCommonFreeDeposit {
    
    @Resource
    private PxzConfigService pxzConfigService;
    
    @Resource
    private FyConfigService fyConfigService;
    
    public PxzCommonRequest<PxzFreeDepositOrderRequest> buildFreeDepositOrderPxzRequest(FreeDepositOrderRequest freeDepositOrderRequest) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(freeDepositOrderRequest.getTenantId());
        if (Objects.isNull(pxzConfig) || StrUtil.isBlank(pxzConfig.getAesKey()) || StrUtil.isBlank(pxzConfig.getMerchantCode())) {
            throw new CustomBusinessException("免押功能未配置相关信息！请联系客服处理");
        }
        
        String orderId = freeDepositOrderRequest.getFreeDepositOrderId();
        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderId);
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeDepositOrderRequest.getPhoneNumber());
        request.setSubject(freeDepositOrderRequest.getSubject());
        request.setRealName(freeDepositOrderRequest.getRealName());
        request.setIdNumber(freeDepositOrderRequest.getIdCard());
        request.setTransId(orderId);
        request.setTransAmt(freeDepositOrderRequest.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);
        
        return query;
    }
    
    
    public PxzCommonRequest<PxzFreeDepositOrderQueryRequest> buildQueryFreeDepositOrderStatusPxzRequest(FreeDepositOrderStatusQuery orderStatusQuery) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(orderStatusQuery.getTenantId());
        if (Objects.isNull(pxzConfig) || StrUtil.isBlank(pxzConfig.getAesKey()) || StrUtil.isBlank(pxzConfig.getMerchantCode())) {
            throw new CustomBusinessException("免押功能未配置相关信息！请联系客服处理");
        }
        
        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderStatusQuery.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(orderStatusQuery.getOrderId());
        query.setData(request);
        return query;
    }
    
    public PxzCommonRequest<PxzFreeDepositUnfreezeRequest> buildUnFreeDepositOrderPxzRequest(FreeDepositOrderStatusQuery orderStatusQuery) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(orderStatusQuery.getTenantId());
        if (Objects.isNull(pxzConfig) || StrUtil.isBlank(pxzConfig.getAesKey()) || StrUtil.isBlank(pxzConfig.getMerchantCode())) {
            throw new CustomBusinessException("免押功能未配置相关信息！请联系客服处理");
        }
        
        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderStatusQuery.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark(orderStatusQuery.getSubject());
        queryRequest.setTransId(orderStatusQuery.getOrderId());
        
        query.setData(queryRequest);
        return query;
    }
    
    
    public FyCommonQuery<FyAuthPayRequest> buildFyAuthPayRequest(FreeDepositOrderRequest orderRequest) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(orderRequest.getTenantId());
        if (Objects.isNull(fyConfig)) {
            throw new CustomBusinessException("蜂云免押功能未配置相关信息！请联系客服处理");
        }
        FyCommonQuery<FyAuthPayRequest> query = new FyCommonQuery<>();
        FyAuthPayRequest request = new FyAuthPayRequest();
        request.setThirdOrderNo(orderRequest.getFreeDepositOrderId());
        request.setMerNo(fyConfig.getMerchantCode());
        request.setStoreId(fyConfig.getStoreCode());
        request.setFqNum(FyConfig.FREE_ORDER_DATE);
        request.setAmount(orderRequest.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue());
        request.setSubject(orderRequest.getSubject());
        
        request.setNotifyUrl(orderRequest.getCallbackUrl());
        request.setEnablePayChannels(FyConstants.PAY_CHANNEL_ZHIMA);
        request.setPayTypes(FyConstants.PAY_TYPES);
        
        AuthPayVars authPayVars = new AuthPayVars();
        authPayVars.setFqFlag("0");
        authPayVars.setUserName(orderRequest.getRealName());
        authPayVars.setMobile(orderRequest.getPhoneNumber());
        authPayVars.setProvinceName("陕西省");
        authPayVars.setCityName("西安市");
        authPayVars.setDistrictName("灞桥区");
        request.setVars(JsonUtil.toJson(authPayVars));
        
        query.setFlowNo(orderRequest.getFreeDepositOrderId());
        query.setFyRequest(request);
        return query;
    }
}
