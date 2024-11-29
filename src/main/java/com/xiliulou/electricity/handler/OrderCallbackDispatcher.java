/**
 * Create date: 2024/7/16
 */

package com.xiliulou.electricity.handler;

import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.enums.CallBackEnums;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.pay.base.request.BaseOrderCallBackResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/16 17:50
 */
@Component
public class OrderCallbackDispatcher {
    
    
    private final Map<String, OrderCallbackStrategy> strategies = new HashMap<>();
    
    @Resource
    private ElectricityTradeOrderService electricityTradeOrderService;
    
    @Resource
    private UnionTradeOrderService unionTradeOrderService;
    
    
    @PostConstruct
    private void initStrategies() {
        strategies.put(CallBackEnums.CAR_RENAL_PACKAGE_ORDER.getDesc(), param -> electricityTradeOrderService.notifyCarRenalPackageOrder(param));
        
        strategies.put(ElectricityTradeOrder.ATTACH_INSURANCE, param -> electricityTradeOrderService.notifyInsuranceOrder(param));
        
        strategies.put(ElectricityTradeOrder.ATTACH_CLOUD_BEAN_RECHARGE, param -> electricityTradeOrderService.notifyCloudBeanRechargeOrder(param));
        
        strategies.put(UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT, param -> unionTradeOrderService.notifyIntegratedPayment(param));
        
        strategies.put(UnionTradeOrder.ATTACH_MEMBERCARD_INSURANCE, param -> unionTradeOrderService.notifyMembercardInsurance(param));
        
        strategies.put(UnionTradeOrder.ATTACH_SERVUCE_FEE, param -> unionTradeOrderService.notifyServiceFee(param));
    
        strategies.put(UnionTradeOrder.ATTACH_INSTALLMENT, param -> unionTradeOrderService.notifyInstallmentPayment(param));
        
        strategies.put(UnionTradeOrder.ATTACH_PLACE_ORDER, param -> unionTradeOrderService.notifyPlaceOrder(param));
    }
    
    /**
     * 调度
     *
     * @param callBackResource
     * @author caobotao.cbt
     * @date 2024/7/16 18:16
     */
    public void dispatch(BaseOrderCallBackResource callBackResource) {
        OrderCallbackStrategy strategy = strategies.get(callBackResource.getAttach());
        if (strategy != null) {
            strategy.execute(callBackResource);
        } else {
//            electricityTradeOrderService.notifyMemberOrder(callBackResource);
        }
    }
    
    
    @FunctionalInterface
    interface OrderCallbackStrategy<T extends BaseOrderCallBackResource> {
        void execute(T callBackResource);
    }
}
