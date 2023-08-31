package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.CarProtocolService;
import com.xiliulou.electricity.service.DepositProtocolService;
import com.xiliulou.electricity.service.OrderProtocolService;
import com.xiliulou.electricity.service.UserNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 4:45 下午
 */
@RestController
public class JsonOuterNoticeController extends BaseController {
    @Autowired
    UserNoticeService userNoticeService;

    @Autowired
    DepositProtocolService depositProtocolService;

    @Autowired
    OrderProtocolService orderProtocolService;

    @Autowired
    CarProtocolService carProtocolService;


    @GetMapping("/outer/userNotice")
    public R queryUserNotice() {

        return userNoticeService.queryUserNotice();
    }


    @GetMapping("/outer/depositProtocol")
    public R queryDepositProtocol() {

        return depositProtocolService.queryDepositProtocol();
    }

    @GetMapping("/outer/orderProtocol")
    public R queryOrderProtocol() {

        return orderProtocolService.queryOrderProtocol();
    }

    @GetMapping("/outer/carRentalAndRefundProtocol")
    public R queryCarRentalAndRefundProtocol() {

        return R.ok(carProtocolService.findProtocolByQuery());
    }

}
