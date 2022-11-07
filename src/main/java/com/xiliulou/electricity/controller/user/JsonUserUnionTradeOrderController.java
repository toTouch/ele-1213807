package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 混合支付(UnionTradeOrder)表控制层
 *
 * @author makejava
 * @since 2022-11-07 10:16:44
 */
@RestController
@Slf4j
public class JsonUserUnionTradeOrderController {
    /**
     * 服务对象
     */
    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    FranchiseeService franchiseeService;

    //缴纳保险
    @PostMapping("/user/payInsuranceAndDeposit")
    public R payDeposit(@RequestBody @Validated(value = CreateGroup.class) InsuranceOrderAdd insuranceOrderAdd, HttpServletRequest request) {
        return insuranceOrderService.createOrder(insuranceOrderAdd, request);
    }




}

