package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
    public R payInsurance(@RequestBody @Validated(value = CreateGroup.class) InsuranceOrderAdd insuranceOrderAdd, HttpServletRequest request) {
        return insuranceOrderService.createOrder(insuranceOrderAdd, request);
    }
    
    //用户查询保险
    @GetMapping(value = "/user/queryInsurance")
    public R queryInsurance() {
        return insuranceOrderService.queryInsurance();
    }
    
    //用户首次联合查询保险
    @GetMapping(value = "/user/homeOneQueryInsurance")
    public R homeOneQueryInsurance(@RequestParam(value = "model", required = false) Integer model, @RequestParam("franchiseeId") Long franchiseeId) {
        return insuranceOrderService.homeOneQueryInsurance(model, franchiseeId);
    }
    
    /**
     * 用户保险订单分页
     */
    @GetMapping(value = "/user/insuranceOrder/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam("type") Integer type) {
        InsuranceOrderQuery query = InsuranceOrderQuery.builder().uid(SecurityUtils.getUid()).size(size).offset(offset).status(InsuranceOrder.STATUS_SUCCESS).type(type)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return insuranceOrderService.queryList(query, false);
    }
    
    
}

