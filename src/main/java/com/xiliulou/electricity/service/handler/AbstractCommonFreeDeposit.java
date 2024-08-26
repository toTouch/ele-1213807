package com.xiliulou.electricity.service.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.FreeDepositConfig;
import com.xiliulou.electricity.constant.FreeDepositConstant;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.pay.deposit.fengyun.constant.FyConstants;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.AuthPayVars;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyAgreementPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyAuthPayRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyHandleFundRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQueryFreezeStatusRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyResult;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositAuthToPayRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositUnfreezeRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

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
    
    @Resource
    private FreeDepositConfig freeDepositConfig;
    
    
    public PxzCommonRequest<PxzFreeDepositOrderRequest> buildFreeDepositOrderPxzRequest(FreeDepositOrderRequest freeDepositOrderRequest) {
        PxzConfig pxzConfig = getPxzConfig(freeDepositOrderRequest.getTenantId());
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
        request.setCallbackUrl(freeDepositConfig.getPxzFreeUrl());
        query.setData(request);
        
        return query;
    }
    
    private PxzConfig getPxzConfig(Integer tenantId) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(pxzConfig) || StrUtil.isBlank(pxzConfig.getAesKey()) || StrUtil.isBlank(pxzConfig.getMerchantCode())) {
            throw new CustomBusinessException("免押功能未配置相关信息！请联系客服处理");
        }
        return pxzConfig;
    }
    
    
    public PxzCommonRequest<PxzFreeDepositOrderQueryRequest> buildQueryFreeDepositOrderStatusPxzRequest(FreeDepositOrderStatusQuery orderStatusQuery) {
        PxzConfig pxzConfig = getPxzConfig(orderStatusQuery.getTenantId());
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
    
    public PxzCommonRequest<PxzFreeDepositUnfreezeRequest> buildUnFreeDepositOrderPxzRequest(UnFreeDepositOrderQuery unFreeDepositOrderQuery) {
        PxzConfig pxzConfig = getPxzConfig(unFreeDepositOrderQuery.getTenantId());
        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(unFreeDepositOrderQuery.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark(unFreeDepositOrderQuery.getSubject());
        queryRequest.setTransId(unFreeDepositOrderQuery.getOrderId());
        
        query.setData(queryRequest);
        return query;
    }
    
    public PxzCommonRequest<PxzFreeDepositAuthToPayRequest> buildAuthPxzRequest(FreeDepositAuthToPayQuery authToPayQuery) {
        PxzConfig pxzConfig = getPxzConfig(authToPayQuery.getTenantId());
        
        PxzCommonRequest<PxzFreeDepositAuthToPayRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(authToPayQuery.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositAuthToPayRequest request = new PxzFreeDepositAuthToPayRequest();
        request.setPayNo(authToPayQuery.getOrderId());
        request.setTransId(authToPayQuery.getOrderId());
        request.setAuthNo(authToPayQuery.getAuthNo());
        request.setTransAmt(authToPayQuery.getPayTransAmt().multiply(BigDecimal.valueOf(100)).longValue());
        query.setData(request);
        return query;
    }
    
    
    public Triple<Boolean, String, Object> pxzResultCheck(PxzCommonRsp rsp, String orderId) {
        if (Objects.isNull(rsp)) {
            log.warn("Pxz ERROR! pxzResultCheck fail! pxzQueryOrderRsp is null! orderId={}", orderId);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        if (!rsp.isSuccess()) {
            log.warn("Pxz ERROR! pxzResultCheck fail! pxzQueryOrderRsp is fail! orderId={}, rsp is {}", orderId, JsonUtil.toJson(rsp));
            return Triple.of(false, "100401", rsp.getRespDesc());
        }
        
        if (Objects.isNull(rsp.getData())) {
            log.warn("Pxz ERROR! pxzResultCheck fail! pxzQueryOrderRsp.data is null! orderId={}, rsp is {}", orderId, JsonUtil.toJson(rsp));
            return Triple.of(false, "100401", rsp.getRespDesc());
        }
        log.info("Pxz Result INFO! pxzResultCheck.result is {}", JsonUtil.toJson(rsp.getData()));
        return Triple.of(true, null, null);
    }
    
    
    public FyCommonQuery<FyAuthPayRequest> buildFyAuthPayRequest(FreeDepositOrderRequest orderRequest) {
        FyConfig fyConfig = getFyConfig(orderRequest.getTenantId());
        
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
        // 产品定义写为西安
        authPayVars.setProvinceName("陕西省");
        authPayVars.setCityName("西安市");
        authPayVars.setDistrictName("未央区");
        request.setVars(JsonUtil.toJson(authPayVars));
        
        query.setFlowNo(orderRequest.getFreeDepositOrderId());
        query.setFyRequest(request);
        return query;
    }
    
    
    public FyCommonQuery<FyQueryFreezeStatusRequest> buildFyFreeDepositStatusRequest(FreeDepositOrderStatusQuery orderStatusQuery) {
        getFyConfig(orderStatusQuery.getTenantId());
        
        FyCommonQuery<FyQueryFreezeStatusRequest> query = new FyCommonQuery<>();
        FyQueryFreezeStatusRequest request = new FyQueryFreezeStatusRequest();
        request.setThirdOrderNo(orderStatusQuery.getOrderId());
        
        query.setFlowNo(orderStatusQuery.getOrderId());
        query.setFyRequest(request);
        return query;
    }
    
    
    public FyCommonQuery<FyHandleFundRequest> buildFyUnFreeRequest(UnFreeDepositOrderQuery orderStatusQuery) {
        getFyConfig(orderStatusQuery.getTenantId());
        
        FyCommonQuery<FyHandleFundRequest> query = new FyCommonQuery<>();
        FyHandleFundRequest request = new FyHandleFundRequest();
        request.setPayNo(orderStatusQuery.getOrderId());
        request.setThirdOrderNo(orderStatusQuery.getOrderId());
        request.setAmount(StrUtil.isNotEmpty(orderStatusQuery.getAmount()) ? Integer.parseInt(orderStatusQuery.getAmount()) : 0);
        request.setSubject(orderStatusQuery.getSubject());
        //  解冻回调地址配置
        request.setNotifyUrl(freeDepositConfig.getFyUnFreeUrl());
        request.setTradeType(FyConstants.HANDLE_FUND_TRADE_TYPE_UNFREEZE);
        
        query.setFlowNo(orderStatusQuery.getOrderId());
        query.setFyRequest(request);
        return query;
    }
    
    public FyCommonQuery<FyAgreementPayRequest> buildFyAgreementPayRequest(FreeDepositAuthToPayQuery payQuery) {
        getFyConfig(payQuery.getTenantId());
        
        FyCommonQuery<FyAgreementPayRequest> query = new FyCommonQuery<>();
        FyAgreementPayRequest request = new FyAgreementPayRequest();
        request.setPayNo(payQuery.getOrderId());
        // todo 协议号
        request.setAgreementNo("");
        request.setTotalAmount(payQuery.getPayTransAmt().multiply(BigDecimal.valueOf(100)).toString());
        request.setSubject(payQuery.getSubject());
        //  解冻回调地址配置
        request.setNotifyUrl(freeDepositConfig.getFyUnFreeUrl());
        
        request.setUserName(payQuery.getUserName());
        request.setMobile(payQuery.getMobile());
        request.setProvinceName("陕西省");
        request.setCityName("西安市");
        
        query.setFlowNo(payQuery.getOrderId());
        query.setFyRequest(request);
        return query;
    }
    
    
    private FyConfig getFyConfig(Integer tenantId) {
        FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(fyConfig)) {
            throw new CustomBusinessException("蜂云免押功能未配置相关信息！请联系客服处理");
        }
        return fyConfig;
    }
    
    public Triple<Boolean, String, Object> fyResultCheck(FyResult result, String orderId) {
        if (Objects.isNull(result)) {
            log.warn("FY ERROR! fyResultCheck fail! result is null!  orderId={}", orderId);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        String code = result.getCode();
        if (!Objects.equals(code, FreeDepositConstant.SUCCESS_CODE)) {
            log.warn("FY ERROR! fyResultCheck fail! result is null!  orderId={}", orderId);
            return Triple.of(false, "100401", result.getMessage());
        }
        
        if (Objects.isNull(result.getFyResponse())) {
            log.warn("FY ERROR! fyResultCheck fail! fyResponse is null!  orderId={}", orderId);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        log.info("FY Result INFO! fyResultCheck.result is {} ", JsonUtil.toJson(result.getFyResponse()));
        return Triple.of(true, null, null);
    }
    
    public Integer fyAuthStatusToPxzStatus(String authStatus) {
        
        if (Objects.equals(authStatus, FreeDepositConstant.FY_INIT)) {
            return FreeDepositOrder.AUTH_INIT;
        }
        
        if (Objects.equals(authStatus, FreeDepositConstant.FY_SUCCESS)) {
            return FreeDepositOrder.AUTH_FROZEN;
        }
        
        if (Objects.equals(authStatus, FreeDepositConstant.FY_CLOSE)) {
            return FreeDepositOrder.AUTH_TIMEOUT;
        }
        throw new CustomBusinessException("蜂云免押查询状态异常");
    }
}
