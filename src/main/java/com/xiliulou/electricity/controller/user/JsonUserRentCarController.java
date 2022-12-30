package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.query.UserRentCarOrderQuery;
import com.xiliulou.electricity.service.RentCarOrderService;
import com.xiliulou.electricity.service.UserCarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 租车
 */
@RestController
@Slf4j
public class JsonUserRentCarController extends BaseController {

    @Autowired
    UserCarService userCarService;
    @Autowired
    RentCarOrderService rentCarOrderService;

    /**
     * 租车&租电下单（混合支付）
     */
    @PostMapping("/user/rentCar/hybridOrder")
    public R rentCarHybridOrder(@RequestBody @Validated RentCarHybridOrderQuery query, HttpServletRequest request) {
        return returnTripleResult(rentCarOrderService.rentCarHybridOrder(query, request));
    }

    /**
     * 扫码租车
     */
    @PostMapping("/user/rentCar/scanQR")
    public R rentCarOrder(@RequestBody @Validated UserRentCarOrderQuery query) {
        return returnTripleResult(rentCarOrderService.rentCarOrder(query));
    }

    /**
     * 查看用户车辆详情
     */
    @GetMapping("user/car/userCar")
    public R userCar(@RequestParam("uid") Long uid) {
        return R.ok(userCarService.selectDetailByUid(uid));
    }


}
