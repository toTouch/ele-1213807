package com.xiliulou.electricity.callback.impl.pxz;


import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.PxzParams;
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
public class PxzAuthPayHandler extends AbstractBusiness<PxzParams.AuthPay> implements PxzSupport<PxzParams.AuthPay> {
    
    
    protected PxzAuthPayHandler(FreeDepositOrderService freeDepositOrderService, FreeDepositAlipayHistoryService freeDepositAlipayHistoryService,
            ApplicationContext applicationContext) {
        super(freeDepositOrderService, freeDepositAlipayHistoryService, applicationContext);
    }
    
    @Override
    public boolean business(Integer business) {
        return FreeBusinessTypeEnum.FREE.getCode().equals(business);
    }
    
    @Override
    public boolean process(BusinessHandler handler,FreeDepositOrder order , PxzParams.AuthPay params) {
        if (!FreeDepositOrder.PAY_STATUS_DEAL_SUCCESS.equals(params.getOrderStatus())){
            //todo  取消代扣订单
            return true;
        }
        return handler.authPay(order);
    }
    
    @Override
    public String orderId(CallbackContext<PxzParams.AuthPay> callbackContext) {
        return callbackContext.getParams().getOrderId();
    }
    
    @Override
    public Integer successCode(PxzParams.AuthPay params) {
        return null;
    }
    
    @Override
    public String authNo(PxzParams.AuthPay params) {
        return null;
    }
    
    @Override
    public Integer payStatus(PxzParams.AuthPay params) {
        return params.getOrderStatus();
    }
    
}
