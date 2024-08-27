package com.xiliulou.electricity.callback.impl.fy;


import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.FyParams;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

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
public class FyWithholdHandler extends AbstractBusiness<FyParams.Withhold> implements FySupport<FyParams.Withhold> {
    
    
    protected FyWithholdHandler(FreeDepositOrderService freeDepositOrderService, FreeDepositAlipayHistoryService freeDepositAlipayHistoryService,
            ApplicationContext applicationContext) {
        super(freeDepositOrderService, freeDepositAlipayHistoryService, applicationContext);
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
                if (!businessHandler.withholdDeposit(order)) {
                    isFailed = true;
                }
            }
        }
        
        return buildContext(isFailed);
    }
    
    @Override
    public String orderId(CallbackContext<FyParams.Withhold> callbackContext) {
        return callbackContext.getParams().getThirdOrderNo();
    }
    
    @Override
    public Integer successCode(FyParams.Withhold params) {
        return null;
    }
    
    @Override
    public String authNo(FyParams.Withhold params) {
        return null;
    }
    
    @Override
    public Integer payStatus(FyParams.Withhold params) {
        return FreeDepositOrder.PAY_STATUS_DEAL_SUCCESS;
    }
    
}
