package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 租车套餐会员期限 Controller
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageMemberTerm")
public class JsonAdminCarRentalPackageMemberTermController extends BasicController {

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

}
