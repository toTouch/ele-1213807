package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 租车套餐订单冻结表 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderFreeze")
public class JsonAdminCarRentalPackageOrderFreezeController extends JsonAdminCarBasicController {

    @Resource
    private CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;

}
