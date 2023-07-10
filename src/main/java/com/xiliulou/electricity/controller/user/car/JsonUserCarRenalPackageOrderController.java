package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 租车套餐订单相关的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackageOrder")
public class JsonUserCarRenalPackageOrderController {

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    /**
     * C端用户购买租车套餐订单
     * @param buyOptModel
     */
    @PostMapping("/buyRentalPackageOrder")
    public R buyRentalPackageOrder(CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request) {
        return carRentalPackageOrderBizService.buyRentalPackageOrder(buyOptModel, request);
    }

}
