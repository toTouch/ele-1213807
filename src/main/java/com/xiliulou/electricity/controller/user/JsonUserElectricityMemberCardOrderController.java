package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
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
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:35
 **/

@Slf4j
@RestController
public class JsonUserElectricityMemberCardOrderController {

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;


    @PostMapping("user/memberCard/payMemberCard")
    public R payMemberCard(@RequestBody @Validated(value = CreateGroup.class) ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request) {
        return electricityMemberCardOrderService.createOrder(electricityMemberCardOrderQuery, request);
    }

    @GetMapping("user/memberCardOrder/list")
    public R queryUserList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
                                    @RequestParam(value = "queryStartTime", required = false) Long queryStartTime, @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        return electricityMemberCardOrderService.queryUserList(offset, size, queryStartTime, queryEndTime);
    }

    @GetMapping("user/memberCardOrder/count")
    public R getMemberCardOrderCount(@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                                     @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        return electricityMemberCardOrderService.getMemberCardOrderCount(uid, queryStartTime, queryEndTime);
    }

    @PutMapping("user/memberCard/openOrDisableMemberCard")
    public R openOrDisableMemberCard(@RequestParam("usableStatus") Integer usableStatus){
        return electricityMemberCardOrderService.openOrDisableMemberCard(usableStatus);
    }


}
