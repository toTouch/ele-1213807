package com.xiliulou.electricity.controller.user;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.FreeServiceFeeOrderService;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author : renhang
 * @description JsonUserFreeServiceFeeOrderController
 * @date : 2025-04-01 17:18
 **/
@RestController
@RequestMapping("user/freeServiceFee")
public class JsonUserFreeServiceFeeOrderController {

    @Resource
    private FreeServiceFeeOrderService freeServiceFeeOrderService;


    /**
     * 获取免押服务费状态
     *
     * @return: @return {@link R }
     */

    @GetMapping("getFreeServiceFeeStatus")
    public R getFreeServiceFeeStatus() {
        Long uid = SecurityUtils.getUid();
        return R.ok(freeServiceFeeOrderService.getFreeServiceFeeStatus(uid));
    }
}
