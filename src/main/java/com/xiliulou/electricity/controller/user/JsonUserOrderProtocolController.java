package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.OrderProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 4:45 下午
 */
@RestController
public class JsonUserOrderProtocolController extends BaseController {
    @Autowired
    OrderProtocolService orderProtocolService;

    @GetMapping("/user/orderProtocol")
    public R queryUserNotice() {

        return orderProtocolService.queryOrderProtocol();
    }



}
