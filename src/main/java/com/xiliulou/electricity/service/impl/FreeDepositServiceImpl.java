package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.mapper.FreeDepositOrderMapper;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FyQueryFreezeStatusRequest;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @ClassName: FreeDepositServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-21 19:11
 */

@Service
@Slf4j
public class FreeDepositServiceImpl implements FreeDepositService {
    
    @Resource
    UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private FreeDepositOrderMapper freeDepositOrderMapper;
    
    @Resource
    PxzConfigService pxzConfigService;
    
    @Resource
    PxzDepositService pxzDepositService;
    
    @Override
    public Triple<Boolean, String, Object> checkExistSuccessFreeDepositOrder(Long uid, FreeDepositUserDTO freeDepositUserDTO) {
        // 获取换电套餐已存在的免押订单信息. 如果不存在或者押金类型为缴纳押金类型则返回
        UserBatteryDeposit batteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(batteryDeposit) || UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT.equals(batteryDeposit.getDepositType())) {
            return Triple.of(false, null, null);
        }
        
        // 获取押金订单记录
        FreeDepositOrder freeDepositOrder = freeDepositOrderMapper.queryByOrderId(batteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            return Triple.of(false, null, null);
        }
        
        // 检查传入的用户信息是否和前一次传入的内容一致，一致返回false,不生成新的
        if (Objects.equals(freeDepositOrder.getRealName(), freeDepositUserDTO.getRealName()) && Objects.equals(freeDepositOrder.getIdCard(), freeDepositUserDTO.getIdCard())
                && Objects.equals(batteryDeposit.getDid(), freeDepositUserDTO.getPackageId())) {
            return Triple.of(true, null, freeDepositOrder);
        }
        
        // 获取上次免押的渠道，查询上次的免押状态
        if (Objects.equals(freeDepositOrder.getChannel(), FreeDepositChannelEnum.PXZ.getChannel())) {
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(pxzConfig) || StrUtil.isBlank(pxzConfig.getAesKey()) || StrUtil.isBlank(pxzConfig.getMerchantCode())) {
                return Triple.of(true, "100400", "免押功能未配置相关信息！请联系客服处理");
            }
            
            // 拍小租进行过免押操作，且已免押成功
            return checkFreeDepositStatusFromPxzV2(batteryDeposit.getOrderId(), pxzConfig);
        }
        if (Objects.equals(freeDepositOrder.getChannel(), FreeDepositChannelEnum.FY.getChannel())) {
            // todo 蜂云免押
            return checkFreeDepositStatusFromFY(batteryDeposit.getOrderId());
        }
        return Triple.of(false, null, null);
    }
    
    
    private Triple<Boolean, String, Object> checkFreeDepositStatusFromFY(String orderId) {
        
        log.info("checkFreeDepositStatusFromFY. orderId = {}, ", orderId);
        
        FyCommonQuery<FyQueryFreezeStatusRequest> query = new FyCommonQuery<>();
        
        FyQueryFreezeStatusRequest request = new FyQueryFreezeStatusRequest();
        request.setThirdOrderNo(orderId);
        query.setFyRequest(request);
        
        // fyDepositService.queryFreezeStatus();
        
        return Triple.of(false, null, null);
    }
    
    
    private Triple<Boolean, String, Object> checkFreeDepositStatusFromPxzV2(String orderId, PxzConfig pxzConfig) {
        
        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderId);
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(orderId);
        query.setData(request);
        
        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("query free deposit status from pxz error!  orderId = {}", orderId, e);
        }
        
        if (Objects.nonNull(pxzQueryOrderRsp) && Objects.nonNull(pxzQueryOrderRsp.getData())) {
            PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
            if (PxzQueryOrderRsp.AUTH_FROZEN.equals(queryOrderRspData.getAuthStatus())) {
                log.info("query free deposit status from pxz success!  orderId = {}", orderId);
                return Triple.of(true, "100400", "免押已成功，请勿重复操作");
            }
        }
        return Triple.of(false, null, null);
    }
    
    
    /**
     * 免押接口，根据次数区分使用拍小租还是蜂云
     *
     * @param orderId
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(String orderId) {
        return null;
    }
    
    
    private Triple<Boolean, String, Object> freeDepositOrderPXZ(FreeDepositOrderRequest freeDepositOrderRequest) {
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(freeDepositOrderRequest.getTenantId());
        if (Objects.isNull(pxzConfig) || StrUtil.isBlank(pxzConfig.getAesKey()) || StrUtil.isBlank(pxzConfig.getMerchantCode())) {
            return Triple.of(true, "100400", "免押功能未配置相关信息！请联系客服处理");
        }
        
        String orderId = freeDepositOrderRequest.getFreeDepositOrderId();
        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderId);
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeDepositOrderRequest.getPhoneNumber());
        request.setSubject("电池免押");
        request.setRealName(freeDepositOrderRequest.getRealName());
        request.setIdNumber(freeDepositOrderRequest.getIdCard());
        request.setTransId(orderId);
        request.setTransAmt(BigDecimal.valueOf(freeDepositOrderRequest.getPayAmount().doubleValue()).multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);
        
        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        if (Objects.isNull(callPxzRsp)) {
            log.error("Pxz ERROR! freeDepositOrder fail! rsp is null!  orderId={}", orderId);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        if (!callPxzRsp.isSuccess()) {
            return Triple.of(false, "100401", callPxzRsp.getRespDesc());
        }
        return Triple.of(true, null, null);
    }
}

