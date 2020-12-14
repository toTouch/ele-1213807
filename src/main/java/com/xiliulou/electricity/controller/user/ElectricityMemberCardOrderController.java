package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:35
 **/

@Slf4j
@RestController
public class ElectricityMemberCardOrderController {

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;


    @GetMapping("user/memberCard/payParams/{memberId}")
    public R payMemberCard(@PathVariable(value = "memberId") Integer memberId, HttpServletRequest request) {
//        Long uid = SecurityUtils.getUid();
//        if (Objects.isNull(uid)) {
//            return R.fail("ELECTRICITY.0001", "未找到用户!");
//        }
//
        Long uid = 222L;
        return electricityMemberCardOrderService.createOrder(uid, memberId, request);
    }

    @GetMapping("user/memberCardOrder/page")
    public R getMemberCardOrderPage(@RequestParam("offset") Long offset, @RequestParam("size") Long size) {
//        Long uid = SecurityUtils.getUid();
//        if (Objects.isNull(uid)) {
//            return R.fail("ELECTRICITY.0001", "未找到用户!");
//        }
        Long uid = 222L;
        return electricityMemberCardOrderService.getMemberCardOrderPage(uid, offset, size);
    }

    @GetMapping("user/memberCardOrder/count")
    public R getMemberCardOrderCount() {
//        Long uid = SecurityUtils.getUid();
//        if (Objects.isNull(uid)) {
//            return R.fail("ELECTRICITY.0001", "未找到用户!");
//        }
        Long uid = 222L;
        return electricityMemberCardOrderService.getMemberCardOrderCount(uid);
    }


}
