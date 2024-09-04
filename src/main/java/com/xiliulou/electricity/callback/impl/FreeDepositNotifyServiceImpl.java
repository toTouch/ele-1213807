package com.xiliulou.electricity.callback.impl;


import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.CallbackHandler;
import com.xiliulou.electricity.callback.FreeDepositNotifyService;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.FyParams;
import com.xiliulou.electricity.dto.callback.PxzParams;
import com.xiliulou.electricity.dto.callback.UnfreeFakeParams;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.utils.FyAesUtil;
import com.xiliulou.pay.deposit.paixiaozu.PxzAesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: This class is FreeDepositNotifyService!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/26
 **/
@Slf4j
@Service
public class FreeDepositNotifyServiceImpl implements FreeDepositNotifyService, CommandLineRunner {
    
    private final ApplicationContext applicationContext;
    
    private final PxzConfigService pxzConfigService;
    
    private final FengYunConfig fengYunConfig;
    
    private final LinkedList<CallbackHandler<?>> handlers = new LinkedList<>();
    
    public FreeDepositNotifyServiceImpl(ApplicationContext applicationContext, PxzConfigService pxzConfigService, FengYunConfig fengYunConfig) {
        this.applicationContext = applicationContext;
        this.pxzConfigService = pxzConfigService;
        this.fengYunConfig = fengYunConfig;
    }
    
    @Override
    @SuppressWarnings("all")
    public Object notify(Integer channel ,Integer business, Integer tenantId , Map<String, Object> params) {
        if (CollectionUtils.isEmpty(handlers)){
            return "";
        }
        log.info("free deposit receive notify params:{}", params);
        Iterator<CallbackHandler<?>> iterator = handlers.iterator();
        
        CallbackContext<?> context = CallbackContext.builder()
                .business(business)
                .channel(channel)
                .tenantId(tenantId)
                .params(params)
                .next(Boolean.TRUE)
                .build();
        
        while (iterator.hasNext()) {
            CallbackHandler<Object> next = (CallbackHandler<Object>)iterator.next();
            if (Objects.isNull(context)){
                return "";
            }
            if (!context.isNext()){
                return context.getParams();
            }
            if (next.support(context.getChannel())) {
                if (next instanceof AbstractBusiness){
                    if (((AbstractBusiness<?>)next).business(context.getBusiness())){
                        ((AbstractBusiness<?>)next).init();
                        context = next.handler((CallbackContext<Object>) context);
                    }
                    continue;
                }
                context = next.handler((CallbackContext<Object>) context);
            }
        }
        return context.getParams();
    }
    
    @Override
    public Object unfreeFakeNotify(UnfreeFakeParams params) throws Exception {
        log.info("unfree fake notify params:{}", params);
        Map<String,Object> notifyParams = new HashMap<>();
        
        if (params.getChannel().equals(FreeDepositChannelEnum.PXZ.getChannel())){
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(params.getTenantId());
            PxzParams.FreeDepositOrUnfree unfree = new PxzParams.FreeDepositOrUnfree();
            PxzParams.FreeDepositOrUnfreeBody unfreeBody = new PxzParams.FreeDepositOrUnfreeBody();
            unfreeBody.setAuthNo(params.getAuthNO());
            unfreeBody.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
            unfreeBody.setTransId(params.getOrderId());
            unfree.setRequestBody(unfreeBody);
            unfree.setRequestHeader(new PxzParams.PxzHeader(System.currentTimeMillis(), ""));
            String encrypt = PxzAesUtils.encrypt(JsonUtil.toJson(unfree), pxzConfig.getAesKey());
            notifyParams.put("body", encrypt);
        }
        if (params.getChannel().equals(FreeDepositChannelEnum.FY.getChannel())){
            FyParams.AuthPayOrUnfree unfree = new FyParams.AuthPayOrUnfree();
            unfree.setTradeType("UNFREEZE");
            unfree.setThirdOrderNo(params.getOrderId());
            unfree.setPayNo(params.getAuthNO());
            String encrypt = FyAesUtil.encrypt(JsonUtil.toJson(unfree), fengYunConfig.getAesKey());
            notifyParams.put("bizContent", encrypt);
        }
        return notify(params.getChannel(), FreeBusinessTypeEnum.UNFREE.getCode(),params.getTenantId(),notifyParams);
    }
    
    @Override
    @SuppressWarnings("all")
    public void run(String... args) throws Exception {
        Map<String, CallbackHandler> type = applicationContext.getBeansOfType(CallbackHandler.class);
        if (MapUtils.isNotEmpty(type)){
            handlers.addAll(type.values().stream()
                    .map(handler -> (CallbackHandler<?> )handler)
                    .sorted(Comparator.comparingInt(CallbackHandler::order))
                    .collect(Collectors.toList()));
        }
    }
}
