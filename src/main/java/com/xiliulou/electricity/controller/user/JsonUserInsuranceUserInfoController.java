package com.xiliulou.electricity.controller.user;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 换电柜保险(InsuranceUserInfo)表服务接口
 *
 * @author makejava
 * @since 2022-11-03 13:37:11
 */
@RestController
@Slf4j
public class JsonUserInsuranceUserInfoController extends BaseController {

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    @Autowired
    RedisService redisService;

    @Autowired
    FranchiseeService franchiseeService;

    //用户查询缴纳保险
    @GetMapping(value = "/user/queryUserInsurance")
    public R queryUserInsurance() {
        return insuranceUserInfoService.queryUserInsurance();
    }

    @GetMapping(value = "/user/queryUserInsuranceV3")
    public R queryUserInsuranceV3(@RequestParam("type") Integer type) {
        return insuranceUserInfoService.queryUserInsurance(SecurityUtils.getUid(), type);
    }

    //用户查询缴纳保险
    @GetMapping(value = "/user/queryInsuranceByStatus")
    public R queryInsuranceByStatus(@RequestParam("status") Integer status, @RequestParam("size") Long size, @RequestParam("offset") Long offset) {
        return insuranceUserInfoService.queryInsuranceByStatus(status,offset,size);
    }


}

