package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-22-13:27
 */
@Slf4j
@RestController
public class JsonUserRentCarMemberCardOrderController extends BaseController {

    @Autowired
    private CarMemberCardOrderService carMemberCardOrderService;

    /**
     * 购买租车套餐
     * @param carMemberCardOrderQuery
     * @param request
     * @return
     */
    @PostMapping("user/rentCar/memberCard/order")
    public R payRentCarMemberCard(@RequestBody @Validated CarMemberCardOrderQuery carMemberCardOrderQuery, HttpServletRequest request) {
        return returnTripleResult(carMemberCardOrderService.payRentCarMemberCard(carMemberCardOrderQuery, request));
    }
}
