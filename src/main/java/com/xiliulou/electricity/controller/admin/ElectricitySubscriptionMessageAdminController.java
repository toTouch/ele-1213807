package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricitySubscriptionMessage;
import com.xiliulou.electricity.service.ElectricitySubscriptionMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 17:56
 **/
@RestControllerAdvice
@Slf4j
public class ElectricitySubscriptionMessageAdminController {
    @Autowired
    ElectricitySubscriptionMessageService electricitySubscriptionMessageService;

    /**
     * 新增
     *
     * @return
     */
    @PostMapping("admin/subscriptionMessage")
    public R saveElectricitySubscriptionMessage(@RequestBody @Validated ElectricitySubscriptionMessage electricitySubscriptionMessage) {
        return electricitySubscriptionMessageService.saveElectricitySubscriptionMessage(electricitySubscriptionMessage);
    }

    /**
     * 修改
     *
     * @return
     */
    @PutMapping("admin/subscriptionMessage")
    public R updateElectricitySubscriptionMessage(@RequestBody @Validated ElectricitySubscriptionMessage electricitySubscriptionMessage) {
        if (Objects.isNull(electricitySubscriptionMessage)) {
            return R.failMsg("请求参数Id不能为空!");
        }
        return electricitySubscriptionMessageService.updateElectricitySubscriptionMessage(electricitySubscriptionMessage);
    }


    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/subscriptionMessage/list")
    public R getElectricityMemberCardPage(@RequestParam(value = "type", required = false) Integer type) {

        return electricitySubscriptionMessageService.getElectricitySubscriptionMessagePage(type);
    }

}
