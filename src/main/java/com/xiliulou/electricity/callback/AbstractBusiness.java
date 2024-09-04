package com.xiliulou.electricity.callback;


import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.entity.FreeDepositOrder.PAY_STATUS_DEAL_SUCCESS;

/**
 * <p>
 * Description: This class is AbstractBusiness!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
@Slf4j
public abstract class AbstractBusiness<T> implements CallbackHandler<T> {
    
    protected static List<BusinessHandler> businessHandlerList;
    
    protected final FreeDepositOrderService freeDepositOrderService;
    
    protected final FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    private final ApplicationContext applicationContext;
    
    protected AbstractBusiness(FreeDepositOrderService freeDepositOrderService, FreeDepositAlipayHistoryService freeDepositAlipayHistoryService, ApplicationContext applicationContext) {
        this.freeDepositOrderService = freeDepositOrderService;
        this.freeDepositAlipayHistoryService = freeDepositAlipayHistoryService;
        this.applicationContext = applicationContext;
    }
    
    public abstract boolean business(Integer business);
    
    public abstract boolean process(BusinessHandler handler,FreeDepositOrder order,T params);
    
    public abstract String orderId(CallbackContext<T> callbackContext);
    
    public abstract Integer successCode(T params);
    
    public abstract String authNo(T params);
    
    public abstract Integer payStatus(T params);
    
    public String payNo(T params){
        return null;
    }
    
    @Override
    public int order() {
        return NumberConstant.ZERO;
    }
    
    
    @Override
    public final CallbackContext<?> handler(CallbackContext<T> callbackContext) {
        String orderId = orderId(callbackContext);
        FreeDepositOrder freeDepositOrder = getFreeDepositOrder(orderId);
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("freeDepositOrder is null, orderId is{}", orderId);
            return success();
        }
        boolean isSuccess = true;
        if (CollectionUtils.isNotEmpty(businessHandlerList)){
            for (BusinessHandler businessHandler : businessHandlerList) {
                if (!businessHandler.support(freeDepositOrder.getDepositType())){
                    continue;
                }
                boolean processed = process(businessHandler,freeDepositOrder, callbackContext.getParams());
                if (processed){
                    updateFreeDepositOrder(freeDepositOrder, callbackContext.getParams());
                    continue;
                }
                isSuccess = false;
            }
        }
        return isSuccess?success():failed();
    }
    
    public void init() {
        if (CollectionUtils.isEmpty(businessHandlerList)){
            businessHandlerList = new ArrayList<>(applicationContext.getBeansOfType(BusinessHandler.class).values());
        }
    }
    
    private FreeDepositOrder getFreeDepositOrder(String orderId) {
        return freeDepositOrderService.selectByOrderId(orderId);
    }
    
    
    protected void updateFreeDepositOrder(FreeDepositOrder freeDepositOrder,T params) {
        String authNo = authNo(params);
        Integer payStatus = payStatus(params);
        Integer successCode = successCode(params);
        String payNo = payNo(params);
        log.info("updateFreeDepositOrder, authNo is {}, payStatus is {},code is {},payNo is {}", authNo, payStatus , successCode,payNo);
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        if (StringUtils.isNotEmpty(authNo)){
            freeDepositOrderUpdate.setAuthNo(authNo);
        }
        if (Objects.nonNull(successCode)){
            freeDepositOrderUpdate.setAuthStatus(successCode(params));
        }
        if (Objects.nonNull(payStatus) ){
            if (StringUtils.isNotEmpty(payNo) && Objects.equals(payStatus, PAY_STATUS_DEAL_SUCCESS)){
                BigDecimal payTransAmt = freeDepositAlipayHistoryService.queryPayTransAmtByPayNo(payNo);
                log.info("payTransAmt is {},payNo is {}", payTransAmt, payNo);
                freeDepositOrderUpdate.setWithheldAmt((BigDecimal.valueOf(freeDepositOrder.getWithheldAmt()).add(payTransAmt).doubleValue()));
                freeDepositOrderUpdate.setPayTransAmt((BigDecimal.valueOf(freeDepositOrder.getPayTransAmt()).subtract(payTransAmt)).doubleValue());
            }
            freeDepositOrderUpdate.setPayStatus(payStatus);
        }
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        log.info("updateFreeDepositOrder, freeDepositOrderUpdate is {}", freeDepositOrderUpdate);
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        if (Objects.nonNull(payStatus)){
            FreeDepositAlipayHistory history = new FreeDepositAlipayHistory();
            history.setOrderId(freeDepositOrder.getOrderId());
            if (StringUtils.isNotEmpty(payNo)){
                history.setAuthPayOrderId(payNo);
            }
            history.setPayStatus(payStatus);
            history.setUpdateTime(System.currentTimeMillis());
//            log.info("updateFreeDepositOrder, history is {}", history);
            freeDepositAlipayHistoryService.updateByPayNoOrOrderId(history);
        }
        
    }
}
