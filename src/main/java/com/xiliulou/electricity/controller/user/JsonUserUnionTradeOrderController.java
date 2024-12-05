package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.constant.PlaceOrderConstant;
import com.xiliulou.electricity.query.BatteryMemberCardAndInsuranceQuery;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.query.PlaceOrderQuery;
import com.xiliulou.electricity.query.ServiceFeePaymentQuery;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.PlaceOrderService;
import com.xiliulou.electricity.service.TradeOrderService;
import com.xiliulou.electricity.ttl.ChannelSourceContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 混合支付(UnionTradeOrder)表控制层
 *
 * @author makejava
 * @since 2022-11-07 10:16:44
 */
@RestController
@Slf4j
public class JsonUserUnionTradeOrderController extends BaseController {
    
    /**
     * 服务对象
     */
    @Autowired
    TradeOrderService tradeOrderService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    private PlaceOrderService placeOrderService;
    
    
    // 集成支付
    @PostMapping("/user/integratedPayment")
    public R payDeposit(@RequestBody IntegratedPaymentAdd integratedPaymentAdd, HttpServletRequest request) {
        integratedPaymentAdd.setPaymentChannel(ChannelSourceContextHolder.get());
        return returnTripleResult(tradeOrderService.integratedPayment(integratedPaymentAdd, request));
    }
    
    /**
     * 电池套餐&保险混合支付
     */
    @PostMapping("/user/payMemberCardAndInsurance")
    public R payMemberCardAndInsurance(@RequestBody @Validated(value = CreateGroup.class) BatteryMemberCardAndInsuranceQuery query, HttpServletRequest request) {
        query.setPaymentChannel(ChannelSourceContextHolder.get());
        return returnTripleResult(tradeOrderService.payMemberCardAndInsurance(query, request));
    }
    
    /**
     * 滞纳金混合支付
     */
    @PostMapping("/user/payServiceFee")
    public R payServiceFee(HttpServletRequest request) {
        return returnTripleResult(tradeOrderService.payServiceFee(request));
    }
    
    /**
     * 分期套餐混合支付接口
     */
    @PostMapping("/user/installmentPayment")
    public R<Object> installmentPayment(@RequestBody InstallmentPayQuery query, HttpServletRequest request) {
        return tradeOrderService.installmentPayment(query, request);
    }
    
    /**
     * 押金、套餐、保险购买下单支付接口
     */
    @PostMapping("user/place/order")
    public R<Object> placeOrder(@RequestBody PlaceOrderQuery query, HttpServletRequest request) {
        // 设置支付类型
        query.setPayType(PlaceOrderConstant.ONLINE_PAYMENT);
        
        return placeOrderService.placeOrder(query, request);
    }
}

