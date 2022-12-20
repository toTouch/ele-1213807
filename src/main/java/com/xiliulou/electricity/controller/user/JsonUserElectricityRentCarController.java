package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.service.RentCarOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租车
 */
@RestController
@Slf4j
public class JsonUserElectricityRentCarController {

    @Autowired
    RentCarOrderService rentCarOrderService;

    /**
     * 扫码租车
     */
    @PostMapping("/user/rentCar/scanQR")
    public R rentCarOrder(@RequestBody RentCarHybridOrderQuery query){
        return R.ok(rentCarOrderService.rentCarOrder(query));
    }



}
