package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆租赁订单 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/rentCar")
public class JsonUserCarRentalOrderController {

    /**
     *
     * @param sn 车辆SN码
     * @return
     */
    public R<Boolean> scanQR(String sn) {

        return null;
    }
}
