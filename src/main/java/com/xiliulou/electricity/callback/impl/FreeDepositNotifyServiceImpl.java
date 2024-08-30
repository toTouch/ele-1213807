package com.xiliulou.electricity.callback.impl;


import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.CallbackHandler;
import com.xiliulou.electricity.callback.FreeDepositNotifyService;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    
    private final LinkedList<CallbackHandler<?>> handlers = new LinkedList<>();
    
    public FreeDepositNotifyServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    @SuppressWarnings("unchecked")
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
                        continue;
                    }
                }
                context = next.handler((CallbackContext<Object>) context);
            }
        }
        return context.getParams();
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
