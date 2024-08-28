package com.xiliulou.electricity.callback.impl.fy;


import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.FyParams;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
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
public class FyFreeDepositHandler extends AbstractBusiness<FyParams.FreeDeposit> implements FySupport<FyParams.FreeDeposit> {
    
    private final FreeDepositDataService freeDepositDataService;
    
    protected FyFreeDepositHandler(FreeDepositOrderService freeDepositOrderService, FreeDepositAlipayHistoryService freeDepositAlipayHistoryService,
            ApplicationContext applicationContext, FreeDepositDataService freeDepositDataService) {
        super(freeDepositOrderService, freeDepositAlipayHistoryService, applicationContext);
        this.freeDepositDataService = freeDepositDataService;
    }
    
    
    @Override
    public boolean business(Integer business) {
        return FreeBusinessTypeEnum.FREE.getCode().equals(business);
    }
    
    @Override
    public boolean process(BusinessHandler handler, FreeDepositOrder order, FyParams.FreeDeposit params) {
        boolean b = handler.freeDeposit(order);
        if (b){
            freeDepositDataService.deductionFyFreeDepositCapacity(order.getTenantId(), 1);
        }
        return b;
    }
    
    @Override
    public String orderId(CallbackContext<FyParams.FreeDeposit> callbackContext) {
        return callbackContext.getParams().getThirdOrderNo();
    }
    
    @Override
    public Integer successCode(FyParams.FreeDeposit params) {
        return FreeDepositOrder.AUTH_FROZEN;
    }
    
    @Override
    public String authNo(FyParams.FreeDeposit params) {
        return params.getAuthNo();
    }
    
    @Override
    public Integer payStatus(FyParams.FreeDeposit params) {
        return null;
    }
    
}
