package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 缴纳保险订单(TInsuranceOrder)表控制层
 *
 * @author makejava
 * @since 2022-11-07 10:16:44
 */
@RestController
@Slf4j
public class JsonUserInsuranceOrderController {
    /**
     * 服务对象
     */
    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    FranchiseeService franchiseeService;

    //缴纳保险
    @PostMapping("/user/payInsurance")
    public R payDeposit(@RequestBody @Validated(value = CreateGroup.class) InsuranceOrderAdd insuranceOrderAdd, HttpServletRequest request) {
        return insuranceOrderService.createOrder(insuranceOrderAdd, request);
    }

    //用户查询保险
    @GetMapping(value = "/user/queryInsurance")
    public R queryDeposit(@RequestParam(value = "franchiseeId") Long franchiseeId) {
        return insuranceOrderService.queryInsurance(franchiseeId);
    }


}

