package com.xiliulou.electricity.callback.impl.fy;


import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.callback.impl.pxz.PxzSupport;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.FyParams;
import com.xiliulou.electricity.dto.callback.PxzParams;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
public class FyFreeOfChargeHandler extends AbstractBusiness<FyParams.FreeOfCharge> implements FySupport<FyParams.FreeOfCharge> {
    
    private final FreeDepositDataService freeDepositDataService;
    
    protected FyFreeOfChargeHandler(FreeDepositOrderService freeDepositOrderService, FreeDepositAlipayHistoryService freeDepositAlipayHistoryService,
            ApplicationContext applicationContext, FreeDepositDataService freeDepositDataService) {
        super(freeDepositOrderService, freeDepositAlipayHistoryService, applicationContext);
        this.freeDepositDataService = freeDepositDataService;
    }
    
    
    @Override
    public boolean business(Integer business) {
        return FreeBusinessTypeEnum.FREE.getCode().equals(business);
    }
    
    @Override
    public CallbackContext<?> process(FreeDepositOrder order) {
        
        boolean isFailed = false;
        
        if (CollectionUtils.isNotEmpty(businessHandlerList)){
            for (BusinessHandler businessHandler : businessHandlerList) {
                if (!businessHandler.freeDeposit(order)) {
                    isFailed = true;
                }
            }
        }
        if (!isFailed){
            freeDepositDataService.deductionFyFreeDepositCapacity(order.getTenantId(), 1);
        }
        return buildContext(isFailed);
    }
    
    @Override
    public String orderId(CallbackContext<FyParams.FreeOfCharge> callbackContext) {
        return callbackContext.getParams().getThirdOrderNo();
    }
    
    @Override
    public Integer successCode(FyParams.FreeOfCharge params) {
        return FreeDepositOrder.AUTH_FROZEN;
    }
    
    @Override
    public String authNo(FyParams.FreeOfCharge params) {
        return params.getAuthNo();
    }
    
    @Override
    public Integer payStatus(FyParams.FreeOfCharge params) {
        return null;
    }
    
}
