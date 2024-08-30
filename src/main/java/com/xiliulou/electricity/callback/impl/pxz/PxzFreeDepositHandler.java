package com.xiliulou.electricity.callback.impl.pxz;


import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.constant.FreeDepositConstant;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.PxzParams;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * <p>
 * Description: This class is PxzFreeOfChargeHandler!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
@Slf4j
@Service
public class PxzFreeDepositHandler extends AbstractBusiness<PxzParams.FreeDepositOrUnfree> implements PxzSupport<PxzParams.FreeDepositOrUnfree> {
    
    private final int[] BUSINESS_ARRAY = {FreeBusinessTypeEnum.FREE.getCode(),4,10,13};
    
    private final FreeDepositDataService freeDepositDataService;
    
    protected PxzFreeDepositHandler(FreeDepositOrderService freeDepositOrderService, FreeDepositAlipayHistoryService freeDepositAlipayHistoryService,
            ApplicationContext applicationContext, FreeDepositDataService freeDepositDataService) {
        super(freeDepositOrderService, freeDepositAlipayHistoryService, applicationContext);
        this.freeDepositDataService = freeDepositDataService;
    }
    
    
    @Override
    public boolean business(Integer business) {
        return Arrays.stream(BUSINESS_ARRAY).anyMatch(item -> item == business);
    }
    
    @Override
    public boolean process(BusinessHandler handler,FreeDepositOrder order,PxzParams.FreeDepositOrUnfree params) {
        
        if ( FreeDepositOrder.AUTH_FROZEN.equals(params.getRequestBody().getAuthStatus()) && FreeDepositOrder.AUTH_FROZEN.equals(order.getAuthStatus())){
            return true;
        }
        
        if (FreeDepositOrder.AUTH_TIMEOUT.equals(params.getRequestBody().getAuthStatus())){
            handler.timeout(order);
            return true;
        }
        
        boolean b = handler.freeDeposit(order);
        
        if (b){
            freeDepositDataService.deductionFreeDepositCapacity(order.getTenantId(),1);
        }
        return b;
    }
    
    @Override
    public String orderId(CallbackContext<PxzParams.FreeDepositOrUnfree> callbackContext) {
        return callbackContext.getParams().getRequestBody().getTransId();
    }
    
    @Override
    public Integer successCode(PxzParams.FreeDepositOrUnfree params) {
        return params.getRequestBody().getAuthStatus();
    }
    
    @Override
    public String authNo(PxzParams.FreeDepositOrUnfree params) {
        return params.getRequestBody().getAuthNo();
    }
    
    @Override
    public Integer payStatus(PxzParams.FreeDepositOrUnfree params) {
        return null;
    }
}
