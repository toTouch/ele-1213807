package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.electricity.service.RentCarOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租车记录
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-21-15:12
 */
@RestController
@Slf4j
public class JsonAdminRentCarOrderController extends BaseController {

    @Autowired
    private RentCarOrderService rentCarOrderService;




}
