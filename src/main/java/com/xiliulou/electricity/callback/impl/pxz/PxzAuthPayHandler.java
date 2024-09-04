package com.xiliulou.electricity.callback.impl.pxz;


import com.xiliulou.electricity.callback.AbstractBusiness;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.PxzParams;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.query.FreeDepositCancelAuthToPayQuery;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.electricity.service.handler.impl.PxzBaseFreeDepositOrderServiceImpl;
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
public class PxzAuthPayHandler extends AbstractBusiness<PxzParams.AuthPay> implements PxzSupport<PxzParams.AuthPay> {
    
    private final PxzBaseFreeDepositOrderServiceImpl pxzBaseFreeDepositOrderService;
    
    private final FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    protected PxzAuthPayHandler(FreeDepositOrderService freeDepositOrderService, FreeDepositAlipayHistoryService freeDepositAlipayHistoryService,
            ApplicationContext applicationContext, PxzBaseFreeDepositOrderServiceImpl pxzBaseFreeDepositOrderService,
            FreeDepositAlipayHistoryService freeDepositAlipayHistoryService1) {
        super(freeDepositOrderService, freeDepositAlipayHistoryService, applicationContext);
        
        this.pxzBaseFreeDepositOrderService = pxzBaseFreeDepositOrderService;
        this.freeDepositAlipayHistoryService = freeDepositAlipayHistoryService1;
    }
    
    @Override
    public boolean business(Integer business) {
        return Objects.equals(business,FreeBusinessTypeEnum.AUTH_PAY.getCode());
    }
    
    @Override
    public boolean process(BusinessHandler handler,FreeDepositOrder order , PxzParams.AuthPay params) {
        if (!FreeDepositOrder.PAY_STATUS_DEAL_SUCCESS.equals(params.getRequestBody().getOrderStatus())){
            return pxzBaseFreeDepositOrderService.cancelAuthPay(
                    FreeDepositCancelAuthToPayQuery.builder().authPayOrderId(params.getRequestBody().getPayNo()).uid(order.getUid()).tenantId(order.getTenantId()).orderId(order.getOrderId())
                            .channel(FreeDepositChannelEnum.PXZ.getChannel()).build());
        }
        return handler.authPay(order);
    }
    
    @Override
    public String orderId(CallbackContext<PxzParams.AuthPay> callbackContext) {
        return freeDepositAlipayHistoryService.queryOrderIdByAuthNo(callbackContext.getParams().getRequestBody().getPayNo());
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
        return params.getRequestBody().getOrderStatus();
    }
    
    @Override
    public String payNo(PxzParams.AuthPay params) {
        return params.getRequestBody().getPayNo();
    }
}
