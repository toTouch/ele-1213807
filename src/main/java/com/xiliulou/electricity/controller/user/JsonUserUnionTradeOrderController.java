package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryMemberCardAndInsuranceQuery;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.query.ServiceFeePaymentQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.TradeOrderService;
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
    
    
    // 集成支付
    @PostMapping("/user/integratedPayment")
    public R payDeposit(@RequestBody IntegratedPaymentAdd integratedPaymentAdd, HttpServletRequest request) {
        return returnTripleResult(tradeOrderService.integratedPayment(integratedPaymentAdd, request));
    }
    
    /**
     * 电池套餐&保险混合支付
     */
    @PostMapping("/user/payMemberCardAndInsurance")
    public R payMemberCardAndInsurance(@RequestBody @Validated(value = CreateGroup.class) BatteryMemberCardAndInsuranceQuery query, HttpServletRequest request) {
        return returnTripleResult(tradeOrderService.payMemberCardAndInsurance(query, request));
    }
    
    /**
     * 滞纳金混合支付
     */
    @PostMapping("/user/payServiceFee")
    public R payServiceFee(@RequestBody ServiceFeePaymentQuery query, HttpServletRequest request) {
        return returnTripleResult(tradeOrderService.payServiceFee(query, request));
    }
}

