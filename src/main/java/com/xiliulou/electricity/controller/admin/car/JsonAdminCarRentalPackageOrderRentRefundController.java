package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.electricity.service.car.CarRentalPackageOrderRentRefundService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderRentRefund")
public class JsonAdminCarRentalPackageOrderRentRefundController extends JsonAdminCarBasicController {

    @Resource
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;

}
